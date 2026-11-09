package com.ofdun.jobfinder.features.auth.domain.service;

import com.ofdun.jobfinder.features.auth.domain.model.TokenPair;
import com.ofdun.jobfinder.features.auth.domain.repository.TokenRepository;
import com.ofdun.jobfinder.features.auth.enums.AccountType;
import com.ofdun.jobfinder.features.auth.mfa.AuthenticationProtection;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class ApplicantAuthService implements AuthService {
    private final AuthenticationProtection protection;
    private final TokenRepository tokens;

    @Override
    public TokenPair login(@NonNull String email, @NonNull String password) {
        return protection.login(AccountType.APPLICANT, email, password);
    }

    @Override
    public TokenPair refreshToken(@NonNull String refreshToken) {
        return protection.refresh(AccountType.APPLICANT, refreshToken);
    }

    @Override
    public void logout(@NonNull String refreshToken) {
        tokens.deleteToken(refreshToken);
    }
}
