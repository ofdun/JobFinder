package com.ofdun.jobfinder.features.auth.domain.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

import com.ofdun.jobfinder.features.api.ApiFailure;
import com.ofdun.jobfinder.features.applicant.domain.model.ApplicantModel;
import com.ofdun.jobfinder.features.applicant.domain.repository.ApplicantRepository;
import com.ofdun.jobfinder.features.auth.domain.jwt.JwtProvider;
import com.ofdun.jobfinder.features.auth.domain.model.TokenPair;
import com.ofdun.jobfinder.features.auth.domain.repository.TokenRepository;
import com.ofdun.jobfinder.features.auth.enums.AccountType;
import com.ofdun.jobfinder.features.auth.mfa.AccountSecurity;
import com.ofdun.jobfinder.features.auth.mfa.AuthenticationProtection;
import com.ofdun.jobfinder.features.auth.mfa.SecurityState;
import com.ofdun.jobfinder.features.auth.mfa.SecurityStateStore;
import com.ofdun.jobfinder.features.auth.mfa.TotpAuthentication;
import com.ofdun.jobfinder.features.auth.mfa.TotpSupport;
import com.ofdun.jobfinder.features.employer.domain.repository.EmployerRepository;
import com.ofdun.jobfinder.features.encrypt.EncryptionService;
import com.ofdun.jobfinder.support.TestDataMother;
import java.time.Clock;
import java.time.Duration;
import java.util.Base64;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ValueOperations;

@ExtendWith(MockitoExtension.class)
@org.junit.jupiter.api.Tag("equivalence")
class ApplicantAuthServiceTest {
    @Test
    @org.junit.jupiter.api.Tag("equivalence")
    void logout_whenStorageFails_propagatesFailure() {
        var failure = new IllegalStateException("storage unavailable");
        doThrow(failure).when(tokenRepository).deleteToken("token");

        var error =
                assertThrows(
                        IllegalStateException.class, () -> applicantAuthService.logout("token"));

        assertSame(failure, error);
    }

    @Mock private EncryptionService encryptionService;

    @Mock private ApplicantRepository applicantRepository;

    @Mock private TokenRepository tokenRepository;

    @Mock private JwtProvider jwtProvider;

    private ApplicantAuthService applicantAuthService;
    private final SecurityState state = new SecurityState();
    private StringRedisTemplate redis;

    @BeforeEach
    void configureAuthentication() {
        redis = mock(StringRedisTemplate.class);
        @SuppressWarnings("unchecked")
        ValueOperations<String, String> values = mock(ValueOperations.class);
        lenient().when(redis.opsForValue()).thenReturn(values);
        var states = mock(SecurityStateStore.class, CALLS_REAL_METHODS);
        lenient()
                .when(states.read(anyString()))
                .thenAnswer(call -> new SecurityStateStore.Snapshot(0, state));
        lenient().when(states.save(anyString(), anyLong(), any())).thenReturn(true);
        var other = mock(EmployerRepository.class);
        var clock = Clock.systemUTC();
        var accounts =
                new AccountSecurity(
                        applicantRepository,
                        other,
                        encryptionService,
                        states,
                        redis,
                        clock,
                        3,
                        90,
                        900);
        var totp = new TotpSupport(Base64.getEncoder().encodeToString(new byte[32]));
        var factors = new TotpAuthentication(accounts, states, totp, clock);
        var protection =
                new AuthenticationProtection(
                        accounts, factors, states, totp, redis, jwtProvider, tokenRepository, 300);
        applicantAuthService = new ApplicantAuthService(protection, tokenRepository);
    }

    @Test
    void login_whenValidCredentials_thenReturnTokenPair() {
        String email = "test@test.com";
        String password = "password";
        Long applicantId = 1L;
        String hashedPassword = "hashedPassword";
        String accessToken = "access";
        String refreshToken = "refresh";
        long duration = 1000L;

        ApplicantModel applicant = mock(ApplicantModel.class);
        when(applicant.getPasswordHash()).thenReturn(hashedPassword);
        when(applicant.getId()).thenReturn(applicantId);
        when(applicantRepository.getApplicantByEmail(email)).thenReturn(Optional.of(applicant));

        when(applicantRepository.getApplicantById(applicantId)).thenReturn(Optional.of(applicant));
        when(tokenRepository.saveToken(eq(refreshToken), eq(applicantId), any(Duration.class)))
                .thenReturn(true);
        when(encryptionService.matches(password, hashedPassword)).thenReturn(true);

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
        when(applicantRepository.getApplicantByEmail(email)).thenReturn(Optional.empty());

        RuntimeException exception =
                assertThrows(
                        RuntimeException.class, () -> applicantAuthService.login(email, password));

        assertEquals(401, ((ApiFailure) exception).getStatus());
        verify(applicantRepository).getApplicantByEmail(email);
        verifyNoInteractions(encryptionService, tokenRepository, jwtProvider);
    }

    @Test
    void login_whenPasswordInvalid_thenThrowsException() {
        String email = "test@test.com";
        String password = "password";
        ApplicantModel applicant = mock(ApplicantModel.class);
        when(applicant.getPasswordHash()).thenReturn("storedHash");
        when(applicantRepository.getApplicantByEmail(email)).thenReturn(Optional.of(applicant));

        when(applicant.getId()).thenReturn(1L);
        when(applicantRepository.getApplicantById(1L)).thenReturn(Optional.of(applicant));
        when(encryptionService.matches(password, "storedHash")).thenReturn(false);

        RuntimeException exception =
                assertThrows(
                        RuntimeException.class, () -> applicantAuthService.login(email, password));

        assertEquals(401, ((ApiFailure) exception).getStatus());
        verify(applicantRepository).getApplicantByEmail(email);
        verify(encryptionService).matches(password, "storedHash");
        verifyNoInteractions(tokenRepository, jwtProvider);
    }

    @Test
    void refreshToken_whenValidToken_thenReturnNewTokenPair() {
        String oldRefreshToken = "oldRefresh";
        Long userId = 1L;
        String newAccess = "newAccess";
        String newRefresh = "newRefresh";

        when(jwtProvider.validateRefreshToken(oldRefreshToken, AccountType.APPLICANT))
                .thenReturn(true);
        var account = TestDataMother.applicant();
        account.setId(userId);
        when(applicantRepository.getApplicantById(userId)).thenReturn(Optional.of(account));
        when(jwtProvider.getUserId(oldRefreshToken)).thenReturn(userId);
        when(redis.opsForValue().getAndDelete(oldRefreshToken)).thenReturn(userId.toString());
        when(jwtProvider.getRefreshTokenExpiration()).thenReturn(60_000L);
        when(tokenRepository.saveToken(eq(newRefresh), eq(userId), any(Duration.class)))
                .thenReturn(true);
        when(jwtProvider.generateAccessToken(AccountType.APPLICANT, userId)).thenReturn(newAccess);
        when(jwtProvider.generateRefreshToken(AccountType.APPLICANT, userId))
                .thenReturn(newRefresh);

        TokenPair tokenPair = applicantAuthService.refreshToken(oldRefreshToken);

        assertEquals(newAccess, tokenPair.accessToken());
        assertEquals(newRefresh, tokenPair.refreshToken());
        verify(redis.opsForValue()).getAndDelete(oldRefreshToken);
        verify(tokenRepository).saveToken(eq(newRefresh), eq(userId), any(Duration.class));
        verifyNoMoreInteractions(tokenRepository);
    }

    @Test
    void refreshToken_whenInvalidToken_thenThrowsException() {
        String token = "invalidToken";
        var account = TestDataMother.applicant();
        account.setId(1L);
        when(applicantRepository.getApplicantById(1L)).thenReturn(Optional.of(account));
        when(jwtProvider.getUserId(token)).thenReturn(1L);
        when(jwtProvider.validateRefreshToken(token, AccountType.APPLICANT)).thenReturn(false);

        RuntimeException exception =
                assertThrows(
                        RuntimeException.class, () -> applicantAuthService.refreshToken(token));

        assertEquals(401, ((ApiFailure) exception).getStatus());
        verify(jwtProvider).validateRefreshToken(token, AccountType.APPLICANT);
        verifyNoInteractions(tokenRepository);
    }

    @Test
    void refreshToken_whenSessionIsOver_thenThrowsException() {
        String token = "expiredToken";
        var account = TestDataMother.applicant();
        account.setId(1L);
        when(applicantRepository.getApplicantById(1L)).thenReturn(Optional.of(account));
        when(jwtProvider.getUserId(token)).thenReturn(1L);
        when(jwtProvider.validateRefreshToken(token, AccountType.APPLICANT)).thenReturn(true);
        when(redis.opsForValue().getAndDelete(token)).thenReturn(null);

        RuntimeException exception =
                assertThrows(
                        RuntimeException.class, () -> applicantAuthService.refreshToken(token));

        assertEquals(401, ((ApiFailure) exception).getStatus());
        verify(jwtProvider).validateRefreshToken(token, AccountType.APPLICANT);
        verify(redis.opsForValue()).getAndDelete(token);
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
