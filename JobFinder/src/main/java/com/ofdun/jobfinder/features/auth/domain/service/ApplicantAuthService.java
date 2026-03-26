package com.ofdun.jobfinder.features.auth.domain.service;

import com.ofdun.jobfinder.features.auth.domain.jwt.JwtProvider;
import com.ofdun.jobfinder.features.auth.domain.model.AccountType;
import com.ofdun.jobfinder.features.auth.domain.model.TokenPair;
import com.ofdun.jobfinder.features.auth.domain.repository.ApplicantAccountRepository;
import com.ofdun.jobfinder.features.auth.domain.repository.TokenRepository;
import com.ofdun.jobfinder.shared.domain.encrypt.EncryptionService;
import lombok.NonNull;
import org.springframework.stereotype.Service;

import java.time.Duration;

@Service
public class ApplicantAuthService implements AuthService {
    EncryptionService encryptionService;
    ApplicantAccountRepository applicantAccountRepository;
    TokenRepository tokenRepository;
    JwtProvider jwtProvider;

    @Override
    @NonNull
    public TokenPair login(String email, String password) {
        var applicant = applicantAccountRepository.findByEmail(email);
        if (applicant == null) {
            throw new RuntimeException("Applicant not found");
        }

        if (!encryptionService.encrypt(password).equals(applicant.getPasswordHash())) {
            throw new RuntimeException("Invalid password");
        }

        var accessToken = jwtProvider.generateAccessToken(AccountType.APPLICANT, applicant.getId());
        var refreshToken = jwtProvider.generateRefreshToken(AccountType.APPLICANT, applicant.getId());

        tokenRepository.saveToken(refreshToken,
                applicant.getId(),
                Duration.ofMillis(jwtProvider.getRefreshTokenExpiration()));

        return new TokenPair(accessToken, refreshToken);
    }

    @Override
    @NonNull
    public TokenPair refreshToken(String refreshToken) {
        if (!jwtProvider.validateToken(refreshToken, AccountType.APPLICANT)) {
            throw new RuntimeException("Refresh token invalid");
        }

        Long userId = tokenRepository.getUserIdByToken(refreshToken);

        if (userId == null) {
            throw new RuntimeException("Session is over or invalid");
        }

        tokenRepository.deleteToken(refreshToken);

        String newAccess = jwtProvider.generateAccessToken(AccountType.APPLICANT, userId);
        String newRefresh = jwtProvider.generateRefreshToken(AccountType.APPLICANT, userId);

        tokenRepository.saveToken(newRefresh, userId, Duration.ofDays(7));

        return new TokenPair(newAccess, newRefresh);
    }

    @Override
    public void logout(@NonNull String refreshToken) {
        tokenRepository.deleteToken(refreshToken);
    }
}
