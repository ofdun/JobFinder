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
    private final EncryptionService encryptionService;
    private final ApplicantAccountRepository applicantAccountRepository;
    private final TokenRepository tokenRepository;
    private final JwtProvider jwtProvider;

    public ApplicantAuthService(@NonNull EncryptionService encryptionService,
                                @NonNull ApplicantAccountRepository applicantAccountRepository,
                                @NonNull TokenRepository tokenRepository,
                                @NonNull JwtProvider jwtProvider) {
        this.encryptionService = encryptionService;
        this.applicantAccountRepository = applicantAccountRepository;
        this.tokenRepository = tokenRepository;
        this.jwtProvider = jwtProvider;
    }

    @Override
    public TokenPair login(@NonNull String email, @NonNull String password) {
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
    public TokenPair refreshToken(@NonNull String refreshToken) {
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
