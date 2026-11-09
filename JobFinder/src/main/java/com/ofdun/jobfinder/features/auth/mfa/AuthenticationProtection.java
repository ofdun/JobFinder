package com.ofdun.jobfinder.features.auth.mfa;

import com.ofdun.jobfinder.features.api.ApiFailure;
import com.ofdun.jobfinder.features.applicant.domain.repository.ApplicantRepository;
import com.ofdun.jobfinder.features.auth.domain.jwt.JwtProvider;
import com.ofdun.jobfinder.features.auth.domain.model.TokenPair;
import com.ofdun.jobfinder.features.auth.domain.repository.TokenRepository;
import com.ofdun.jobfinder.features.auth.enums.AccountType;
import com.ofdun.jobfinder.features.employer.domain.repository.EmployerRepository;
import com.ofdun.jobfinder.features.encrypt.EncryptionService;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

@Service
public class AuthenticationProtection {
    private final ApplicantRepository applicants;
    private final EmployerRepository employers;
    private final EncryptionService passwords;
    private final SecurityStateStore states;
    private final TotpSupport totp;
    private final StringRedisTemplate redis;
    private final JwtProvider jwt;
    private final TokenRepository tokens;
    private final int attemptLimit;
    private final long passwordLifetime;
    private final Duration challengeLifetime;
    private final long lockoutDuration;
    private final java.time.Clock clock;

    public AuthenticationProtection(
            ApplicantRepository applicants,
            EmployerRepository employers,
            EncryptionService passwords,
            SecurityStateStore states,
            TotpSupport totp,
            StringRedisTemplate redis,
            JwtProvider jwt,
            TokenRepository tokens,
            java.time.Clock clock,
            @Value("${app.auth.attempt-limit:5}") int attemptLimit,
            @Value("${app.auth.password-max-age-days:90}") long passwordDays,
            @Value("${app.auth.challenge-seconds:300}") long challengeSeconds,
            @Value("${app.auth.lockout-seconds:900}") long lockoutSeconds) {
        if (attemptLimit < 1 || passwordDays < 1 || challengeSeconds < 1 || lockoutSeconds < 1)
            throw new IllegalArgumentException("Invalid authentication policy");
        this.applicants = applicants;
        this.employers = employers;
        this.passwords = passwords;
        this.states = states;
        this.totp = totp;
        this.redis = redis;
        this.jwt = jwt;
        this.tokens = tokens;
        this.clock = clock;
        this.attemptLimit = attemptLimit;
        this.passwordLifetime = Duration.ofDays(passwordDays).toMillis();
        this.challengeLifetime = Duration.ofSeconds(challengeSeconds);
        this.lockoutDuration = Duration.ofSeconds(lockoutSeconds).toMillis();
    }

    private record Account(AccountType type, long id, String email, String hash) {}

    private record Challenge(Account account, String stamp, String purpose) {}

    private Optional<Account> find(AccountType type, String email) {
        if (type == AccountType.APPLICANT)
            return applicants
                    .getApplicantByEmail(email)
                    .map(a -> new Account(type, a.getId(), a.getEmail(), a.getPasswordHash()));
        return employers
                .getEmployerByEmail(email)
                .map(a -> new Account(type, a.getId(), a.getEmail(), a.getPasswordHash()));
    }

    private Account account(AccountType type, long id) {
        if (type == AccountType.APPLICANT)
            return applicants
                    .getApplicantById(id)
                    .map(a -> new Account(type, id, a.getEmail(), a.getPasswordHash()))
                    .orElseThrow(this::invalid);
        return employers
                .getEmployerById(id)
                .map(a -> new Account(type, id, a.getEmail(), a.getPasswordHash()))
                .orElseThrow(this::invalid);
    }

    public String sessionStamp(AccountType type, long id) {
        var account = account(type, id);
        return TotpSupport.digest(account.hash() + ":" + states.get(type, id).sessionVersion);
    }

    public boolean sessionAllowed(AccountType type, long id, String stamp) {
        var state = states.get(type, id);
        return !state.locked && !expired(state) && sessionStamp(type, id).equals(stamp);
    }

    public TokenPair login(AccountType type, String email, String password) {
        var account = find(type, email).orElseThrow(this::invalid);
        String stamp = sessionStamp(type, account.id());
        checkPassword(account, password);
        var state = states.get(type, account.id());
        if (state.secret != null) throw challenge(account, "TOTP");
        if (expired(state)) throw challenge(account, "PASSWORD_CHANGE");
        states.update(
                type,
                account.id(),
                s -> {
                    s.failures = 0;
                    return null;
                });
        return issue(account, stamp);
    }

    public TokenPair verify(String token, String code) {
        var challenge = readChallenge(token, "TOTP");
        var account = challenge.account();
        verifyFactor(account, code);
        consumeChallenge(token);
        if (expired(states.get(account.type(), account.id())))
            throw challenge(account, "PASSWORD_CHANGE");
        return issue(account, challenge.stamp());
    }

    public TokenPair refresh(AccountType type, String token) {
        String stamp;
        try {
            stamp = sessionStamp(type, jwt.getUserId(token));
        } catch (RuntimeException ex) {
            throw invalid();
        }
        if (!jwt.validateRefreshToken(token, type)) throw invalid();
        String id = redis.opsForValue().getAndDelete(token);
        if (id == null || !id.equals(jwt.getUserId(token).toString())) throw invalid();
        return issue(account(type, Long.parseLong(id)), stamp);
    }

    public Map<String, Object> status(AccountType type, long id) {
        var state = states.get(type, id);
        return Map.of(
                "enabled",
                state.secret != null,
                "recoveryCodesRemaining",
                state.recoveryHashes.size(),
                "passwordExpiresAt",
                state.passwordChangedAt + passwordLifetime);
    }

    public Map<String, String> setup(AccountType type, long id, String password) {
        var account = account(type, id);
        checkCurrentPassword(account, password);
        String secret = totp.newSecret();
        String encrypted = totp.encrypt(secret, states.key(type, id));
        states.update(
                type,
                id,
                state -> {
                    if (state.secret != null)
                        throw new ApiFailure(
                                "MFA_ENABLED", 409, "Двухфакторная аутентификация уже подключена");
                    state.pendingSecret = encrypted;
                    state.pendingUntil = clock.millis() + Duration.ofMinutes(10).toMillis();
                    return null;
                });
        return Map.of("secret", secret, "qrCode", totp.qr(secret, account.email()));
    }

    public List<String> enable(AccountType type, long id, String code) {
        List<String> backup = new ArrayList<>();
        for (int i = 0; i < 10; i++) backup.add(totp.randomToken());
        int result =
                states.update(
                        type,
                        id,
                        state -> {
                            if (state.locked) return 423;
                            if (state.secret != null
                                    || state.pendingSecret == null
                                    || state.pendingUntil < clock.millis()) return 401;
                            long step =
                                    totp.matchedStep(
                                            totp.decrypt(state.pendingSecret, states.key(type, id)),
                                            code,
                                            clock.millis(),
                                            -1);
                            if (step < 0) return failure(state);
                            state.secret = state.pendingSecret;
                            state.pendingSecret = null;
                            state.pendingUntil = 0;
                            state.lastStep = step;
                            state.failures = 0;
                            state.sessionVersion++;
                            state.recoveryHashes =
                                    new ArrayList<>(
                                            backup.stream().map(TotpSupport::digest).toList());
                            return 200;
                        });
        requireSuccess(result, type, id);
        return backup;
    }

    public void disable(AccountType type, long id, String password, String code) {
        var account = account(type, id);
        checkCurrentPassword(account, password);
        verifyFactor(account, code);
        states.update(
                type,
                id,
                state -> {
                    state.secret = null;
                    state.pendingSecret = null;
                    state.recoveryHashes.clear();
                    state.sessionVersion++;
                    return null;
                });
    }

    public void changePassword(
            AccountType type, long id, String oldPassword, String newPassword, String code) {
        var account = account(type, id);
        checkCurrentPassword(account, oldPassword);
        validateNewPassword(account, newPassword);
        if (states.get(type, id).secret != null) verifyFactor(account, code);
        replacePassword(account, newPassword);
    }

    public void renewPassword(String token, String newPassword) {
        var challenge = readChallenge(token, "PASSWORD_CHANGE");
        validateNewPassword(challenge.account(), newPassword);
        consumeChallenge(token);
        replacePassword(challenge.account(), newPassword);
    }

    public void recover(
            AccountType type,
            String email,
            String password,
            String backup,
            String newPassword,
            boolean resetAuthenticator) {
        var account = find(type, email).orElseThrow(this::invalid);
        if (!passwords.matches(password, account.hash())) throw invalid();
        String rateKey = "auth:recovery:" + states.key(type, account.id());
        Long count = redis.opsForValue().increment(rateKey);
        if (Long.valueOf(1).equals(count)) redis.expire(rateKey, Duration.ofHours(1));
        if (count == null || count > 10)
            throw new ApiFailure(
                    "RECOVERY_LIMIT", 429, "Превышен лимит восстановления. Повторите позже");
        validateNewPassword(account, newPassword);
        int result =
                states.update(
                        type,
                        account.id(),
                        state -> {
                            if (state.secret == null
                                    || !state.recoveryHashes.remove(TotpSupport.digest(backup)))
                                return 401;
                            state.sessionVersion++;
                            return 200;
                        });
        requireSuccess(result, type, account.id());
        replacePassword(account, newPassword);
        if (resetAuthenticator) {
            states.update(
                    type,
                    account.id(),
                    state -> {
                        state.secret = null;
                        state.pendingSecret = null;
                        state.recoveryHashes.clear();
                        state.sessionVersion++;
                        return null;
                    });
        }
        redis.delete(rateKey);
    }

    public void checkProfilePasswordChange(AccountType type, long id, String newHash) {
        if (!account(type, id).hash().equals(newHash) && states.get(type, id).secret != null)
            throw new ApiFailure(
                    "PASSWORD_CHANGE_REQUIRED", 409, "Используйте защищённую смену пароля");
    }

    public void remove(AccountType type, long id) {
        states.delete(states.key(type, id));
        redis.delete("auth:recovery:" + states.key(type, id));
    }

    private void verifyFactor(Account account, String code) {
        int result =
                states.update(
                        account.type(),
                        account.id(),
                        state -> {
                            if (state.locked) return 423;
                            if (state.secret == null) return 401;
                            long step =
                                    totp.matchedStep(
                                            totp.decrypt(
                                                    state.secret,
                                                    states.key(account.type(), account.id())),
                                            code,
                                            clock.millis(),
                                            state.lastStep);
                            if (step < 0) return failure(state);
                            state.lastStep = step;
                            state.failures = 0;
                            return 200;
                        });
        requireSuccess(result, account.type(), account.id());
    }

    private void checkPassword(Account account, String password) {
        if (!passwords.matches(password, account.hash())) throw invalid();
        checkUnlocked(account);
    }

    private void checkCurrentPassword(Account account, String password) {
        try {
            checkPassword(account, password);
        } catch (ApiFailure ex) {
            if (ex.getStatus() == 401)
                throw new ApiFailure(
                        "INVALID_CURRENT_PASSWORD",
                        401,
                        "Неверный текущий пароль. Введите пароль, с которым вы входите в аккаунт");
            throw ex;
        }
    }

    private void checkUnlocked(Account account) {
        if (!states.get(account.type(), account.id()).locked) return;
        boolean locked =
                states.update(
                        account.type(),
                        account.id(),
                        state -> {
                            if (state.locked && state.lockedUntil == 0)
                                state.lockedUntil = clock.millis() + lockoutDuration;
                            if (state.locked
                                    && state.lockedUntil > 0
                                    && clock.millis() >= state.lockedUntil) {
                                state.locked = false;
                                state.lockedUntil = 0;
                                state.failures = 0;
                            }
                            return state.locked;
                        });
        if (locked) requireSuccess(423, account.type(), account.id());
    }

    private int failure(SecurityState state) {
        if (state.locked) return 423;
        if (++state.failures >= attemptLimit) {
            state.locked = true;
            state.lockedUntil = clock.millis() + lockoutDuration;
            state.sessionVersion++;
            return 423;
        }
        return 401;
    }

    private boolean expired(SecurityState state) {
        return clock.millis() - state.passwordChangedAt >= passwordLifetime;
    }

    private AuthenticationChallenge challenge(Account account, String purpose) {
        String token = totp.randomToken();
        redis.opsForValue()
                .set(
                        "auth:challenge:" + TotpSupport.digest(token),
                        account.type()
                                + ":"
                                + account.id()
                                + ":"
                                + sessionStamp(account.type(), account.id())
                                + ":"
                                + purpose,
                        challengeLifetime);
        return new AuthenticationChallenge(token, purpose);
    }

    private Challenge readChallenge(String token, String purpose) {
        String value = redis.opsForValue().get("auth:challenge:" + TotpSupport.digest(token));
        if (value == null) throw invalid();
        String[] parts = value.split(":");
        var account = account(AccountType.valueOf(parts[0]), Long.parseLong(parts[1]));
        if (!purpose.equals(parts[3])) throw invalid();
        checkUnlocked(account);
        if (!sessionStamp(account.type(), account.id()).equals(parts[2])) throw invalid();
        return new Challenge(account, parts[2], purpose);
    }

    private void consumeChallenge(String token) {
        if (redis.opsForValue().getAndDelete("auth:challenge:" + TotpSupport.digest(token)) == null)
            throw invalid();
    }

    private TokenPair issue(Account account, String stamp) {
        if (!sessionAllowed(account.type(), account.id(), stamp)) throw invalid();
        String access = jwt.generateAccessToken(account.type(), account.id());
        String refresh = jwt.generateRefreshToken(account.type(), account.id());
        if (!sessionAllowed(account.type(), account.id(), stamp)) throw invalid();
        if (!Boolean.TRUE.equals(
                tokens.saveToken(
                        refresh, account.id(), Duration.ofMillis(jwt.getRefreshTokenExpiration()))))
            throw new ApiFailure("SESSION_UNAVAILABLE", 503, "Не удалось создать сессию");
        return new TokenPair(access, refresh);
    }

    private void validateNewPassword(Account account, String password) {
        if (password == null
                || password.length() < 12
                || password.getBytes(java.nio.charset.StandardCharsets.UTF_8).length > 72
                || passwords.matches(password, account.hash()))
            throw new ApiFailure(
                    "PASSWORD_POLICY",
                    400,
                    "Новый пароль должен отличаться от старого и содержать от 12 символов до 72"
                            + " байт");
    }

    private void replacePassword(Account account, String password) {
        String hash = passwords.encrypt(password);
        states.update(
                account.type(),
                account.id(),
                state -> {
                    state.sessionVersion++;
                    return null;
                });
        if (account.type() == AccountType.APPLICANT) {
            var model = applicants.getApplicantById(account.id()).orElseThrow(this::invalid);
            model.setPasswordHash(hash);
            applicants.updateApplicant(model);
        } else {
            var model = employers.getEmployerById(account.id()).orElseThrow(this::invalid);
            model.setPasswordHash(hash);
            employers.updateEmployer(model);
        }
        states.update(
                account.type(),
                account.id(),
                state -> {
                    state.passwordChangedAt = clock.millis();
                    state.failures = 0;
                    state.locked = false;
                    state.lockedUntil = 0;
                    return null;
                });
    }

    private ApiFailure invalid() {
        return new ApiFailure("UNAUTHENTICATED", 401, "Неверные данные аутентификации");
    }

    private void requireSuccess(int status, AccountType type, long id) {
        if (status == 423) {
            var state = states.get(type, id);
            if (state.lockedUntil > 0) {
                long seconds = Math.max(1, (state.lockedUntil - clock.millis() + 999) / 1000);
                throw new ApiFailure(
                        "ACCOUNT_LOCKED",
                        423,
                        "Слишком много неверных попыток. Повторите вход через "
                                + seconds
                                + " сек.");
            }
            throw new ApiFailure(
                    "ACCOUNT_LOCKED",
                    423,
                    "Аккаунт заблокирован. Используйте восстановление доступа");
        }
        if (status != 200) throw invalid();
    }
}
