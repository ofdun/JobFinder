package com.ofdun.jobfinder.features.auth.domain.service;

import com.ofdun.jobfinder.features.auth.domain.jwt.JwtProvider;
import com.ofdun.jobfinder.features.auth.domain.model.TokenPair;
import com.ofdun.jobfinder.features.auth.domain.repository.EmployerAccountRepository;
import com.ofdun.jobfinder.features.auth.domain.repository.TokenRepository;
import com.ofdun.jobfinder.shared.auth.enums.AccountType;
import com.ofdun.jobfinder.shared.encrypt.EncryptionService;
import java.time.Duration;
import lombok.NonNull;
import org.springframework.stereotype.Service;

@Service
public class EmployerAuthService implements AuthService {
    private final EncryptionService encryptionService;
    private final EmployerAccountRepository employerAccountRepository;
    private final TokenRepository tokenRepository;
    private final JwtProvider jwtProvider;

    public EmployerAuthService(
            @NonNull EncryptionService encryptionService,
            @NonNull EmployerAccountRepository employerAccountRepository,
            @NonNull TokenRepository tokenRepository,
            @NonNull JwtProvider jwtProvider) {
        this.encryptionService = encryptionService;
        this.employerAccountRepository = employerAccountRepository;
        this.tokenRepository = tokenRepository;
        this.jwtProvider = jwtProvider;
    }

    @Override
    public TokenPair login(@NonNull String email, @NonNull String password) {
        var applicant = employerAccountRepository.findByEmail(email);
        if (applicant == null) {
            throw new RuntimeException("Applicant not found");
        }

        if (!encryptionService.encrypt(password).equals(applicant.getPasswordHash())) {
            throw new RuntimeException("Invalid password");
        }

        var accessToken = jwtProvider.generateAccessToken(AccountType.EMPLOYER, applicant.getId());
        var refreshToken =
                jwtProvider.generateRefreshToken(AccountType.EMPLOYER, applicant.getId());

        tokenRepository.saveToken(
                refreshToken,
                applicant.getId(),
                Duration.ofMillis(jwtProvider.getRefreshTokenExpiration()));

        return new TokenPair(accessToken, refreshToken);
    }

    @Override
    public TokenPair refreshToken(@NonNull String refreshToken) {
        if (!jwtProvider.validateToken(refreshToken, AccountType.EMPLOYER)) {
            throw new RuntimeException("Refresh token invalid");
        }

        Long userId = tokenRepository.getUserIdByToken(refreshToken);

        if (userId == null) {
            throw new RuntimeException("Session is over or invalid");
        }

        tokenRepository.deleteToken(refreshToken);

        String newAccess = jwtProvider.generateAccessToken(AccountType.EMPLOYER, userId);
        String newRefresh = jwtProvider.generateRefreshToken(AccountType.EMPLOYER, userId);

        tokenRepository.saveToken(newRefresh, userId, Duration.ofDays(7));

        return new TokenPair(newAccess, newRefresh);
    }

    @Override
    public void logout(@NonNull String refreshToken) {
        tokenRepository.deleteToken(refreshToken);
    }
}
