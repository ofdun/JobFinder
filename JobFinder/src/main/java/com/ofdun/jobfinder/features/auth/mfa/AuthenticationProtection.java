package com.ofdun.jobfinder.features.auth.mfa;

import com.ofdun.jobfinder.features.api.ApiFailure;
import com.ofdun.jobfinder.features.auth.domain.jwt.JwtProvider;
import com.ofdun.jobfinder.features.auth.domain.model.TokenPair;
import com.ofdun.jobfinder.features.auth.domain.repository.TokenRepository;
import com.ofdun.jobfinder.features.auth.enums.AccountType;
import com.ofdun.jobfinder.features.auth.mfa.AccountSecurity.Account;
import java.time.Duration;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

@Service
public class AuthenticationProtection {
    private final AccountSecurity accounts;
    private final TotpAuthentication factors;
    private final SecurityStateStore states;
    private final TotpSupport totp;
    private final StringRedisTemplate redis;
    private final JwtProvider jwt;
    private final TokenRepository tokens;
    private final Duration challengeLifetime;

    public AuthenticationProtection(
            AccountSecurity accounts,
            TotpAuthentication factors,
            SecurityStateStore states,
            TotpSupport totp,
            StringRedisTemplate redis,
            JwtProvider jwt,
            TokenRepository tokens,
            @Value("${app.auth.challenge-seconds:300}") long challengeSeconds) {
        if (challengeSeconds < 1) throw new IllegalArgumentException("Invalid challenge lifetime");
        this.accounts = accounts;
        this.factors = factors;
        this.states = states;
        this.totp = totp;
        this.redis = redis;
        this.jwt = jwt;
        this.tokens = tokens;
        this.challengeLifetime = Duration.ofSeconds(challengeSeconds);
    }

    private record Challenge(Account account, String stamp) {}

    public TokenPair login(AccountType type, String email, String password) {
        var account = accounts.find(type, email).orElseThrow(AccountSecurity::invalid);
        String stamp = accounts.sessionStamp(type, account.id());
        accounts.checkPassword(account, password);
        accounts.checkUnlocked(account);
        var state = states.get(type, account.id());
        if (state.secret != null) throw challenge(account, "TOTP");
        if (accounts.expired(state)) throw challenge(account, "PASSWORD_CHANGE");
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
        factors.verifyFactor(account, code);
        consumeChallenge(token);
        if (accounts.expired(states.get(account.type(), account.id())))
            throw challenge(account, "PASSWORD_CHANGE");
        return issue(account, challenge.stamp());
    }

    public TokenPair refresh(AccountType type, String token) {
        String stamp;
        try {
            stamp = accounts.sessionStamp(type, jwt.getUserId(token));
        } catch (RuntimeException ex) {
            throw AccountSecurity.invalid();
        }
        if (!jwt.validateRefreshToken(token, type)) throw AccountSecurity.invalid();
        String id = redis.opsForValue().getAndDelete(token);
        if (id == null || !id.equals(jwt.getUserId(token).toString()))
            throw AccountSecurity.invalid();
        return issue(accounts.account(type, Long.parseLong(id)), stamp);
    }

    public void changePassword(
            AccountType type, long id, String oldPassword, String newPassword, String code) {
        var account = accounts.account(type, id);
        accounts.checkCurrentPassword(account, oldPassword);
        accounts.validateNewPassword(account, newPassword);
        if (states.get(type, id).secret != null) factors.verifyFactor(account, code);
        accounts.replacePassword(account, newPassword);
    }

    public void renewPassword(String token, String newPassword) {
        var challenge = readChallenge(token, "PASSWORD_CHANGE");
        accounts.validateNewPassword(challenge.account(), newPassword);
        consumeChallenge(token);
        accounts.replacePassword(challenge.account(), newPassword);
    }

    public void recover(
            AccountType type,
            String email,
            String password,
            String backup,
            String newPassword,
            boolean resetAuthenticator) {
        var account = accounts.find(type, email).orElseThrow(AccountSecurity::invalid);
        accounts.checkPassword(account, password);
        String rateKey = "auth:recovery:" + states.key(type, account.id());
        Long count = redis.opsForValue().increment(rateKey);
        if (Long.valueOf(1).equals(count)) redis.expire(rateKey, Duration.ofHours(1));
        if (count == null || count > 10)
            throw new ApiFailure(
                    "RECOVERY_LIMIT", 429, "Превышен лимит восстановления. Повторите позже");
        accounts.validateNewPassword(account, newPassword);
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
        accounts.requireSuccess(result, type, account.id());
        accounts.replacePassword(account, newPassword);
        if (resetAuthenticator) factors.reset(type, account.id());
        redis.delete(rateKey);
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
                                + accounts.sessionStamp(account.type(), account.id())
                                + ":"
                                + purpose,
                        challengeLifetime);
        return new AuthenticationChallenge(token, purpose);
    }

    private Challenge readChallenge(String token, String purpose) {
        String value = redis.opsForValue().get("auth:challenge:" + TotpSupport.digest(token));
        if (value == null) throw AccountSecurity.invalid();
        String[] parts = value.split(":");
        var account = accounts.account(AccountType.valueOf(parts[0]), Long.parseLong(parts[1]));
        if (!purpose.equals(parts[3])) throw AccountSecurity.invalid();
        accounts.checkUnlocked(account);
        if (!accounts.sessionStamp(account.type(), account.id()).equals(parts[2]))
            throw AccountSecurity.invalid();
        return new Challenge(account, parts[2]);
    }

    private void consumeChallenge(String token) {
        if (redis.opsForValue().getAndDelete("auth:challenge:" + TotpSupport.digest(token)) == null)
            throw AccountSecurity.invalid();
    }

    private TokenPair issue(Account account, String stamp) {
        if (!accounts.sessionAllowed(account.type(), account.id(), stamp))
            throw AccountSecurity.invalid();
        String access = jwt.generateAccessToken(account.type(), account.id());
        String refresh = jwt.generateRefreshToken(account.type(), account.id());
        if (!accounts.sessionAllowed(account.type(), account.id(), stamp))
            throw AccountSecurity.invalid();
        if (!Boolean.TRUE.equals(
                tokens.saveToken(
                        refresh, account.id(), Duration.ofMillis(jwt.getRefreshTokenExpiration()))))
            throw new ApiFailure("SESSION_UNAVAILABLE", 503, "Не удалось создать сессию");
        return new TokenPair(access, refresh);
    }
}
