package com.ofdun.jobfinder.features.auth.domain.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

import com.ofdun.jobfinder.features.auth.domain.jwt.JwtProvider;
import com.ofdun.jobfinder.features.auth.domain.model.ApplicantAccountModel;
import com.ofdun.jobfinder.features.auth.domain.model.TokenPair;
import com.ofdun.jobfinder.features.auth.domain.repository.ApplicantAccountRepository;
import com.ofdun.jobfinder.features.auth.domain.repository.TokenRepository;
import com.ofdun.jobfinder.shared.auth.enums.AccountType;
import com.ofdun.jobfinder.shared.encrypt.EncryptionService;
import java.time.Duration;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class ApplicantAuthServiceTest {

    @Mock private EncryptionService encryptionService;

    @Mock private ApplicantAccountRepository applicantAccountRepository;

    @Mock private TokenRepository tokenRepository;

    @Mock private JwtProvider jwtProvider;

    @InjectMocks private ApplicantAuthService applicantAuthService;

    @Test
    void login_whenValidCredentials_thenReturnTokenPair() {
        String email = "test@test.com";
        String password = "password";
        Long applicantId = 1L;
        String hashedPassword = "hashedPassword";
        String accessToken = "access";
        String refreshToken = "refresh";
        long duration = 1000L;

        ApplicantAccountModel applicant = mock(ApplicantAccountModel.class);
        when(applicant.getPasswordHash()).thenReturn(hashedPassword);
        when(applicant.getId()).thenReturn(applicantId);
        when(applicantAccountRepository.findByEmail(email)).thenReturn(applicant);
        when(encryptionService.encrypt(password)).thenReturn(hashedPassword);
        when(jwtProvider.generateAccessToken(AccountType.APPLICANT, applicantId))
                .thenReturn(accessToken);
        when(jwtProvider.generateRefreshToken(AccountType.APPLICANT, applicantId))
                .thenReturn(refreshToken);
        when(jwtProvider.getRefreshTokenExpiration()).thenReturn(duration);

        TokenPair tokenPair = applicantAuthService.login(email, password);

        assertEquals(accessToken, tokenPair.accessToken());
        assertEquals(refreshToken, tokenPair.refreshToken());
        verify(tokenRepository).saveToken(eq(refreshToken), eq(applicantId), any(Duration.class));
        verifyNoMoreInteractions(tokenRepository);
    }

    @Test
    void login_whenApplicantNotFound_thenThrowsException() {
        String email = "test@test.com";
        String password = "password";
        when(applicantAccountRepository.findByEmail(email)).thenReturn(null);

        RuntimeException exception =
                assertThrows(
                        RuntimeException.class, () -> applicantAuthService.login(email, password));

        assertEquals("Applicant with string " + email + " already exists", exception.getMessage());
        verify(applicantAccountRepository).findByEmail(email);
        verifyNoInteractions(encryptionService, tokenRepository, jwtProvider);
    }

    @Test
    void login_whenPasswordInvalid_thenThrowsException() {
        String email = "test@test.com";
        String password = "password";
        ApplicantAccountModel applicant = mock(ApplicantAccountModel.class);
        when(applicant.getPasswordHash()).thenReturn("storedHash");
        when(applicantAccountRepository.findByEmail(email)).thenReturn(applicant);
        when(encryptionService.encrypt(password)).thenReturn("differentHash");

        RuntimeException exception =
                assertThrows(
                        RuntimeException.class, () -> applicantAuthService.login(email, password));

        assertEquals("Password is invalid", exception.getMessage());
        verify(applicantAccountRepository).findByEmail(email);
        verify(encryptionService).encrypt(password);
        verifyNoInteractions(tokenRepository, jwtProvider);
    }

    @Test
    void refreshToken_whenValidToken_thenReturnNewTokenPair() {
        String oldRefreshToken = "oldRefresh";
        Long userId = 1L;
        String newAccess = "newAccess";
        String newRefresh = "newRefresh";

        when(jwtProvider.validateToken(oldRefreshToken, AccountType.APPLICANT)).thenReturn(true);
        when(tokenRepository.getUserIdByToken(oldRefreshToken)).thenReturn(userId);
        when(jwtProvider.generateAccessToken(AccountType.APPLICANT, userId)).thenReturn(newAccess);
        when(jwtProvider.generateRefreshToken(AccountType.APPLICANT, userId))
                .thenReturn(newRefresh);

        TokenPair tokenPair = applicantAuthService.refreshToken(oldRefreshToken);

        assertEquals(newAccess, tokenPair.accessToken());
        assertEquals(newRefresh, tokenPair.refreshToken());
        verify(tokenRepository).deleteToken(oldRefreshToken);
        verify(tokenRepository).saveToken(eq(newRefresh), eq(userId), any(Duration.class));
        verifyNoMoreInteractions(tokenRepository);
    }

    @Test
    void refreshToken_whenInvalidToken_thenThrowsException() {
        String token = "invalidToken";
        when(jwtProvider.validateToken(token, AccountType.APPLICANT)).thenReturn(false);

        RuntimeException exception =
                assertThrows(
                        RuntimeException.class, () -> applicantAuthService.refreshToken(token));

        assertEquals("Refresh token is invalid", exception.getMessage());
        verify(jwtProvider).validateToken(token, AccountType.APPLICANT);
        verifyNoInteractions(tokenRepository);
    }

    @Test
    void refreshToken_whenSessionIsOver_thenThrowsException() {
        String token = "expiredToken";
        when(jwtProvider.validateToken(token, AccountType.APPLICANT)).thenReturn(true);
        when(tokenRepository.getUserIdByToken(token)).thenReturn(null);

        RuntimeException exception =
                assertThrows(
                        RuntimeException.class, () -> applicantAuthService.refreshToken(token));

        assertEquals("Session is over", exception.getMessage());
        verify(jwtProvider).validateToken(token, AccountType.APPLICANT);
        verify(tokenRepository).getUserIdByToken(token);
        verify(tokenRepository, never()).deleteToken(anyString());
        verifyNoMoreInteractions(tokenRepository);
    }

    @Test
    void logout_whenValidToken_thenDeletesToken() {
        String token = "token";

        applicantAuthService.logout(token);

        verify(tokenRepository).deleteToken(token);
        verifyNoMoreInteractions(tokenRepository);
    }
}
