package com.ofdun.jobfinder.features.auth.domain.service;

import com.ofdun.jobfinder.features.applicant.exception.ApplicantNotFoundException;
import com.ofdun.jobfinder.features.auth.domain.jwt.JwtProvider;
import com.ofdun.jobfinder.features.auth.domain.model.TokenPair;
import com.ofdun.jobfinder.features.auth.domain.repository.ApplicantAccountRepository;
import com.ofdun.jobfinder.features.auth.domain.repository.TokenRepository;
import com.ofdun.jobfinder.features.auth.enums.AccountType;
import com.ofdun.jobfinder.features.auth.exception.InvalidPasswordException;
import com.ofdun.jobfinder.features.auth.exception.InvalidRefreshTokenException;
import com.ofdun.jobfinder.features.auth.exception.SessionIsOverException;
import com.ofdun.jobfinder.features.encrypt.EncryptionService;

import lombok.NonNull;
import lombok.RequiredArgsConstructor;

import org.springframework.stereotype.Service;

import java.time.Duration;

@Service
@RequiredArgsConstructor
public class ApplicantAuthService implements AuthService {
    @org.springframework.beans.factory.annotation.Autowired
    private com.ofdun.jobfinder.features.auth.mfa.AuthenticationProtection protection;

    private final EncryptionService encryptionService;
    private final ApplicantAccountRepository applicantAccountRepository;
    private final TokenRepository tokenRepository;
    private final JwtProvider jwtProvider;

    @Override
    public TokenPair login(@NonNull String email, @NonNull String password) {
        if (protection != null) return protection.login(AccountType.APPLICANT, email, password);
        var applicant =
                applicantAccountRepository
                        .findByEmail(email)
                        .orElseThrow(() -> new ApplicantNotFoundException(email));

        if (!encryptionService.matches(password, applicant.getPasswordHash())) {
            throw new InvalidPasswordException();
        }

        var accessToken = jwtProvider.generateAccessToken(AccountType.APPLICANT, applicant.getId());
        var refreshToken =
                jwtProvider.generateRefreshToken(AccountType.APPLICANT, applicant.getId());

        tokenRepository.saveToken(
                refreshToken,
                applicant.getId(),
                Duration.ofMillis(jwtProvider.getRefreshTokenExpiration()));

        return new TokenPair(accessToken, refreshToken);
    }

    @Override
    public TokenPair refreshToken(@NonNull String refreshToken) {
        if (protection != null) return protection.refresh(AccountType.APPLICANT, refreshToken);
        if (!jwtProvider.validateRefreshToken(refreshToken, AccountType.APPLICANT)) {
            throw new InvalidRefreshTokenException();
        }

        Long userId = tokenRepository.getUserIdByToken(refreshToken);

        if (userId == null) {
            throw new SessionIsOverException();
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
