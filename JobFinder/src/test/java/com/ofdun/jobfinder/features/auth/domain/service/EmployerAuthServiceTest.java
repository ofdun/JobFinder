package com.ofdun.jobfinder.features.auth.domain.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

import com.ofdun.jobfinder.features.auth.domain.jwt.JwtProvider;
import com.ofdun.jobfinder.features.auth.domain.model.EmployerAccountModel;
import com.ofdun.jobfinder.features.auth.domain.model.TokenPair;
import com.ofdun.jobfinder.features.auth.domain.repository.EmployerAccountRepository;
import com.ofdun.jobfinder.features.auth.domain.repository.TokenRepository;
import com.ofdun.jobfinder.features.auth.enums.AccountType;
import com.ofdun.jobfinder.features.encrypt.EncryptionService;
import java.time.Duration;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class EmployerAuthServiceTest {

    @Mock private EncryptionService encryptionService;

    @Mock private EmployerAccountRepository employerAccountRepository;

    @Mock private TokenRepository tokenRepository;

    @Mock private JwtProvider jwtProvider;

    @InjectMocks private EmployerAuthService employerAuthService;

    @Test
    void login_whenValidCredentials_thenReturnTokenPair() {
        String email = "test@test.com";
        String password = "password";
        Long employerId = 1L;
        String hashedPassword = "hashedPassword";
        String accessToken = "access";
        String refreshToken = "refreshToken";
        long duration = 1000L;

        EmployerAccountModel employer = mock(EmployerAccountModel.class);
        when(employer.getPasswordHash()).thenReturn(hashedPassword);
        when(employer.getId()).thenReturn(employerId);
        when(employerAccountRepository.findByEmail(email)).thenReturn(employer);
        when(encryptionService.encrypt(password)).thenReturn(hashedPassword);
        when(jwtProvider.generateAccessToken(AccountType.EMPLOYER, employerId))
                .thenReturn(accessToken);
        when(jwtProvider.generateRefreshToken(AccountType.EMPLOYER, employerId))
                .thenReturn(refreshToken);
        when(jwtProvider.getRefreshTokenExpiration()).thenReturn(duration);

        TokenPair tokenPair = employerAuthService.login(email, password);

        assertEquals(accessToken, tokenPair.accessToken());
        assertEquals(refreshToken, tokenPair.refreshToken());
        verify(tokenRepository).saveToken(eq(refreshToken), eq(employerId), any(Duration.class));
        verifyNoMoreInteractions(tokenRepository);
    }

    @Test
    void login_whenEmployerNotFound_thenThrowsException() {
        String email = "test@test.com";
        String password = "password";
        when(employerAccountRepository.findByEmail(email)).thenReturn(null);

        RuntimeException exception =
                assertThrows(
                        RuntimeException.class, () -> employerAuthService.login(email, password));

        assertEquals("Applicant not found", exception.getMessage());
        verify(employerAccountRepository).findByEmail(email);
        verifyNoInteractions(encryptionService, tokenRepository, jwtProvider);
    }

    @Test
    void login_whenPasswordInvalid_thenThrowsException() {
        String email = "test@test.com";
        String password = "password";
        EmployerAccountModel employer = mock(EmployerAccountModel.class);
        when(employer.getPasswordHash()).thenReturn("storedHash");
        when(employerAccountRepository.findByEmail(email)).thenReturn(employer);
        when(encryptionService.encrypt(password)).thenReturn("differentHash");

        RuntimeException exception =
                assertThrows(
                        RuntimeException.class, () -> employerAuthService.login(email, password));

        assertEquals("Invalid password", exception.getMessage());
        verify(employerAccountRepository).findByEmail(email);
        verify(encryptionService).encrypt(password);
        verifyNoInteractions(tokenRepository, jwtProvider);
    }

    @Test
    void refreshToken_whenValidToken_thenReturnNewTokenPair() {
        String oldRefreshToken = "oldRefresh";
        Long userId = 1L;
        String newAccess = "newAccess";
        String newRefresh = "newRefresh";

        when(jwtProvider.validateToken(oldRefreshToken, AccountType.EMPLOYER)).thenReturn(true);
        when(tokenRepository.getUserIdByToken(oldRefreshToken)).thenReturn(userId);
        when(jwtProvider.generateAccessToken(AccountType.EMPLOYER, userId)).thenReturn(newAccess);
        when(jwtProvider.generateRefreshToken(AccountType.EMPLOYER, userId)).thenReturn(newRefresh);

        TokenPair tokenPair = employerAuthService.refreshToken(oldRefreshToken);

        assertEquals(newAccess, tokenPair.accessToken());
        assertEquals(newRefresh, tokenPair.refreshToken());
        verify(tokenRepository).deleteToken(oldRefreshToken);
        verify(tokenRepository).saveToken(eq(newRefresh), eq(userId), any(Duration.class));
        verifyNoMoreInteractions(tokenRepository);
    }

    @Test
    void refreshToken_whenInvalidToken_thenThrowsException() {
        String token = "invalidToken";
        when(jwtProvider.validateToken(token, AccountType.EMPLOYER)).thenReturn(false);

        RuntimeException exception =
                assertThrows(RuntimeException.class, () -> employerAuthService.refreshToken(token));

        assertEquals("Refresh token invalid", exception.getMessage());
        verify(jwtProvider).validateToken(token, AccountType.EMPLOYER);
        verifyNoInteractions(tokenRepository);
    }

    @Test
    void refreshToken_whenSessionIsOver_thenThrowsException() {
        String token = "expiredToken";
        when(jwtProvider.validateToken(token, AccountType.EMPLOYER)).thenReturn(true);
        when(tokenRepository.getUserIdByToken(token)).thenReturn(null);

        RuntimeException exception =
                assertThrows(RuntimeException.class, () -> employerAuthService.refreshToken(token));

        assertEquals("Session is over or invalid", exception.getMessage());
        verify(jwtProvider).validateToken(token, AccountType.EMPLOYER);
        verify(tokenRepository).getUserIdByToken(token);
        verify(tokenRepository, never()).deleteToken(anyString());
        verifyNoMoreInteractions(tokenRepository);
    }

    @Test
    void logout_whenValidToken_thenDeletesToken() {
        String token = "token";

        employerAuthService.logout(token);

        verify(tokenRepository).deleteToken(token);
        verifyNoMoreInteractions(tokenRepository);
    }
}
