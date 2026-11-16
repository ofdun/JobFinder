package com.ofdun.jobfinder.features.auth.domain.service;

import com.ofdun.jobfinder.features.auth.domain.model.TokenPair;
import com.ofdun.jobfinder.features.auth.domain.repository.TokenRepository;
import com.ofdun.jobfinder.features.auth.enums.AccountType;
import com.ofdun.jobfinder.features.auth.mfa.AuthenticationProtection;
import io.micrometer.observation.annotation.Observed;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class EmployerAuthService implements AuthService {
    private final AuthenticationProtection protection;
    private final TokenRepository tokens;

    @Override
    @Observed(
            name = "jobfinder.auth.service.login",
            contextualName = "Вход в аккаунт",
            lowCardinalityKeyValues = {"feature", "auth", "layer", "service"})
    public TokenPair login(@NonNull String email, @NonNull String password) {
        return protection.login(AccountType.EMPLOYER, email, password);
    }

    @Override
    @Observed(
            name = "jobfinder.auth.service.refreshToken",
            contextualName = "Обновление токенов",
            lowCardinalityKeyValues = {"feature", "auth", "layer", "service"})
    public TokenPair refreshToken(@NonNull String refreshToken) {
        return protection.refresh(AccountType.EMPLOYER, refreshToken);
    }

    @Override
    @Observed(
            name = "jobfinder.auth.service.logout",
            contextualName = "Завершение сессии",
            lowCardinalityKeyValues = {"feature", "auth", "layer", "service"})
    public void logout(@NonNull String refreshToken) {
        tokens.deleteToken(refreshToken);
    }
}
