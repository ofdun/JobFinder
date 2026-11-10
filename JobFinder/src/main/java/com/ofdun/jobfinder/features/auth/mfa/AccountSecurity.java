package com.ofdun.jobfinder.features.auth.mfa;

import com.ofdun.jobfinder.features.api.ApiFailure;
import com.ofdun.jobfinder.features.applicant.domain.repository.ApplicantRepository;
import com.ofdun.jobfinder.features.auth.enums.AccountType;
import com.ofdun.jobfinder.features.employer.domain.repository.EmployerRepository;
import com.ofdun.jobfinder.features.encrypt.EncryptionService;
import java.nio.charset.StandardCharsets;
import java.time.Clock;
import java.time.Duration;
import java.util.Map;
import java.util.Optional;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

@Service
public class AccountSecurity {
    private final ApplicantRepository applicants;
    private final EmployerRepository employers;
    private final EncryptionService passwords;
    private final SecurityStateStore states;
    private final StringRedisTemplate redis;
    private final Clock clock;
    private final int attemptLimit;
    private final long passwordLifetime;
    private final long lockoutDuration;

    public AccountSecurity(
            ApplicantRepository applicants,
            EmployerRepository employers,
            EncryptionService passwords,
            SecurityStateStore states,
            StringRedisTemplate redis,
            Clock clock,
            @Value("${app.auth.attempt-limit:5}") int attemptLimit,
            @Value("${app.auth.password-max-age-days:90}") long passwordDays,
            @Value("${app.auth.lockout-seconds:900}") long lockoutSeconds) {
        if (attemptLimit < 1 || passwordDays < 1 || lockoutSeconds < 1)
            throw new IllegalArgumentException("Invalid authentication policy");
        this.applicants = applicants;
        this.employers = employers;
        this.passwords = passwords;
        this.states = states;
        this.redis = redis;
        this.clock = clock;
        this.attemptLimit = attemptLimit;
        this.passwordLifetime = Duration.ofDays(passwordDays).toMillis();
        this.lockoutDuration = Duration.ofSeconds(lockoutSeconds).toMillis();
    }

    record Account(AccountType type, long id, String email, String hash) {}

    Optional<Account> find(AccountType type, String email) {
        if (type == AccountType.APPLICANT)
            return applicants
                    .getApplicantByEmail(email)
                    .map(a -> new Account(type, a.getId(), a.getEmail(), a.getPasswordHash()));
        return employers
                .getEmployerByEmail(email)
                .map(a -> new Account(type, a.getId(), a.getEmail(), a.getPasswordHash()));
    }

    Account account(AccountType type, long id) {
        if (type == AccountType.APPLICANT)
            return applicants
                    .getApplicantById(id)
                    .map(a -> new Account(type, id, a.getEmail(), a.getPasswordHash()))
                    .orElseThrow(AccountSecurity::invalid);
        return employers
                .getEmployerById(id)
                .map(a -> new Account(type, id, a.getEmail(), a.getPasswordHash()))
                .orElseThrow(AccountSecurity::invalid);
    }

    public String sessionStamp(AccountType type, long id) {
        var account = account(type, id);
        return TotpSupport.digest(account.hash() + ":" + states.get(type, id).sessionVersion);
    }

    public boolean sessionAllowed(AccountType type, long id, String stamp) {
        var state = states.get(type, id);
        return !state.locked && !expired(state) && sessionStamp(type, id).equals(stamp);
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

    public void checkProfilePasswordChange(AccountType type, long id, String newHash) {
        if (!account(type, id).hash().equals(newHash) && states.get(type, id).secret != null)
            throw new ApiFailure(
                    "PASSWORD_CHANGE_REQUIRED", 409, "Используйте защищённую смену пароля");
    }

    public void remove(AccountType type, long id) {
        states.delete(states.key(type, id));
        redis.delete("auth:recovery:" + states.key(type, id));
    }

    void checkPassword(Account account, String password) {
        if (!passwords.matches(password, account.hash())) throw invalid();
    }

    void checkCurrentPassword(Account account, String password) {
        try {
            checkPassword(account, password);
            checkUnlocked(account);
        } catch (ApiFailure ex) {
            if (ex.getStatus() == 401)
                throw new ApiFailure(
                        "INVALID_CURRENT_PASSWORD",
                        401,
                        "Неверный текущий пароль. Введите пароль, с которым вы входите в аккаунт");
            throw ex;
        }
    }

    void checkUnlocked(Account account) {
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

    int failure(SecurityState state) {
        if (state.locked) return 423;
        if (++state.failures >= attemptLimit) {
            state.locked = true;
            state.lockedUntil = clock.millis() + lockoutDuration;
            state.sessionVersion++;
            return 423;
        }
        return 401;
    }

    boolean expired(SecurityState state) {
        return clock.millis() - state.passwordChangedAt >= passwordLifetime;
    }

    void validateNewPassword(Account account, String password) {
        if (password == null
                || password.length() < 12
                || password.getBytes(StandardCharsets.UTF_8).length > 72
                || passwords.matches(password, account.hash()))
            throw new ApiFailure(
                    "PASSWORD_POLICY",
                    400,
                    "Новый пароль должен отличаться от старого и содержать от 12 символов до 72"
                            + " байт");
    }

    void replacePassword(Account account, String password) {
        String hash = passwords.encrypt(password);
        states.update(
                account.type(),
                account.id(),
                state -> {
                    state.sessionVersion++;
                    return null;
                });
        if (account.type() == AccountType.APPLICANT) {
            var model =
                    applicants.getApplicantById(account.id()).orElseThrow(AccountSecurity::invalid);
            model.setPasswordHash(hash);
            applicants.updateApplicant(model);
        } else {
            var model =
                    employers.getEmployerById(account.id()).orElseThrow(AccountSecurity::invalid);
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

    static ApiFailure invalid() {
        return new ApiFailure("UNAUTHENTICATED", 401, "Неверные данные аутентификации");
    }

    void requireSuccess(int status, AccountType type, long id) {
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
