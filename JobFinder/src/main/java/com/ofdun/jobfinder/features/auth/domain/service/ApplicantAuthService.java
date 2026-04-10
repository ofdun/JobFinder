package com.ofdun.jobfinder.features.auth.domain.service;

import com.ofdun.jobfinder.features.applicant.exception.ApplicantAlreadyExistsException;
import com.ofdun.jobfinder.features.auth.domain.jwt.JwtProvider;
import com.ofdun.jobfinder.features.auth.domain.model.TokenPair;
import com.ofdun.jobfinder.features.auth.domain.repository.ApplicantAccountRepository;
import com.ofdun.jobfinder.features.auth.domain.repository.TokenRepository;
import com.ofdun.jobfinder.features.auth.exception.InvalidPasswordException;
import com.ofdun.jobfinder.features.auth.exception.InvalidRefreshTokenException;
import com.ofdun.jobfinder.features.auth.exception.SessionIsOverException;
import com.ofdun.jobfinder.features.auth.enums.AccountType;
import com.ofdun.jobfinder.features.encrypt.EncryptionService;
import java.time.Duration;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class ApplicantAuthService implements AuthService {
    private final EncryptionService encryptionService;
    private final ApplicantAccountRepository applicantAccountRepository;
    private final TokenRepository tokenRepository;
    private final JwtProvider jwtProvider;

    @Override
    public TokenPair login(@NonNull String email, @NonNull String password) {
        var applicant = applicantAccountRepository.findByEmail(email)
                .orElseThrow(() -> new ApplicantAlreadyExistsException(email));

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
        if (!jwtProvider.validateToken(refreshToken, AccountType.APPLICANT)) {
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
