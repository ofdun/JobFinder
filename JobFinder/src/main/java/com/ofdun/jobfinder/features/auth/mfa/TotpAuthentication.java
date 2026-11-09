package com.ofdun.jobfinder.features.auth.mfa;

import com.ofdun.jobfinder.features.api.ApiFailure;
import com.ofdun.jobfinder.features.auth.enums.AccountType;
import com.ofdun.jobfinder.features.auth.mfa.AccountSecurity.Account;
import java.time.Clock;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class TotpAuthentication {
    private final AccountSecurity accounts;
    private final SecurityStateStore states;
    private final TotpSupport totp;
    private final Clock clock;

    public Map<String, String> setup(AccountType type, long id, String password) {
        var account = accounts.account(type, id);
        accounts.checkCurrentPassword(account, password);
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
                            if (step < 0) return accounts.failure(state);
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
        accounts.requireSuccess(result, type, id);
        return backup;
    }

    public void disable(AccountType type, long id, String password, String code) {
        var account = accounts.account(type, id);
        accounts.checkCurrentPassword(account, password);
        verifyFactor(account, code);
        reset(type, id);
    }

    void verifyFactor(Account account, String code) {
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
                            if (step < 0) return accounts.failure(state);
                            state.lastStep = step;
                            state.failures = 0;
                            return 200;
                        });
        accounts.requireSuccess(result, account.type(), account.id());
    }

    void reset(AccountType type, long id) {
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
}
