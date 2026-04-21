package com.ofdun.jobfinder.features.auth.domain.service;

import com.ofdun.jobfinder.features.employer.exception.EmployerNotFoundException;
import com.ofdun.jobfinder.features.auth.domain.jwt.JwtProvider;
import com.ofdun.jobfinder.features.auth.domain.model.TokenPair;
import com.ofdun.jobfinder.features.auth.domain.repository.EmployerAccountRepository;
import com.ofdun.jobfinder.features.auth.domain.repository.TokenRepository;
import com.ofdun.jobfinder.features.auth.enums.AccountType;
import com.ofdun.jobfinder.features.auth.exception.InvalidPasswordException;
import com.ofdun.jobfinder.features.auth.exception.InvalidRefreshTokenException;
import com.ofdun.jobfinder.features.auth.exception.SessionIsOverException;
import com.ofdun.jobfinder.features.encrypt.EncryptionService;
import java.time.Duration;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class EmployerAuthService implements AuthService {
    private final EncryptionService encryptionService;
    private final EmployerAccountRepository employerAccountRepository;
    private final TokenRepository tokenRepository;
    private final JwtProvider jwtProvider;

    @Override
    public TokenPair login(@NonNull String email, @NonNull String password) {
        var employer = employerAccountRepository.findByEmail(email);
        if (employer == null) {
            throw new EmployerNotFoundException(email);
        }

        if (!encryptionService.matches(password, employer.getPasswordHash())) {
            throw new InvalidPasswordException();
        }

        var accessToken = jwtProvider.generateAccessToken(AccountType.EMPLOYER, employer.getId());
        var refreshToken =
                jwtProvider.generateRefreshToken(AccountType.EMPLOYER, employer.getId());

        tokenRepository.saveToken(
                refreshToken,
                employer.getId(),
                Duration.ofMillis(jwtProvider.getRefreshTokenExpiration()));

        return new TokenPair(accessToken, refreshToken);
    }

    @Override
    public TokenPair refreshToken(@NonNull String refreshToken) {
        if (!jwtProvider.validateToken(refreshToken, AccountType.EMPLOYER)) {
            throw new InvalidRefreshTokenException();
        }

        Long userId = tokenRepository.getUserIdByToken(refreshToken);

        if (userId == null) {
            throw new SessionIsOverException();
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
