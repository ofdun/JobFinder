package com.ofdun.jobfinder.features.auth.mfa;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import com.ofdun.jobfinder.features.api.ApiFailure;
import com.ofdun.jobfinder.features.applicant.domain.repository.ApplicantRepository;
import com.ofdun.jobfinder.features.auth.domain.jwt.JwtProvider;
import com.ofdun.jobfinder.features.auth.domain.repository.TokenRepository;
import com.ofdun.jobfinder.features.auth.enums.AccountType;
import com.ofdun.jobfinder.features.employer.domain.repository.EmployerRepository;
import com.ofdun.jobfinder.features.encrypt.EncryptionService;
import com.ofdun.jobfinder.support.TestDataMother;
import java.time.Clock;
import java.time.Duration;
import java.util.Base64;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.EnumSource;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ValueOperations;

class AuthenticationProtectionTest {
    private final SecurityState state = new SecurityState();
    private final StringRedisTemplate redis = mock(StringRedisTemplate.class);
    private final TotpSupport totp =
            new TotpSupport(Base64.getEncoder().encodeToString(new byte[32]));

    private AuthenticationProtection protection(AccountType type) {
        var applicants = mock(ApplicantRepository.class);
        var employers = mock(EmployerRepository.class);
        var applicant = TestDataMother.applicant();
        applicant.setId(1L);
        applicant.setEmail("account@example.test");
        applicant.setPasswordHash("hash");
        var employer = TestDataMother.employer();
        employer.setId(1L);
        employer.setEmail("account@example.test");
        employer.setPasswordHash("hash");
        when(applicants.getApplicantByEmail("account@example.test"))
                .thenReturn(Optional.of(applicant));
        when(applicants.getApplicantById(1L)).thenReturn(Optional.of(applicant));
        when(employers.getEmployerByEmail("account@example.test"))
                .thenReturn(Optional.of(employer));
        when(employers.getEmployerById(1L)).thenReturn(Optional.of(employer));
        var passwords = mock(EncryptionService.class);
        when(passwords.matches("correct-password", "hash")).thenReturn(true);
        var states = mock(SecurityStateStore.class, CALLS_REAL_METHODS);
        when(states.read(anyString()))
                .thenAnswer(call -> new SecurityStateStore.Snapshot(0, state));
        when(states.save(anyString(), anyLong(), any())).thenReturn(true);
        var jwt = mock(JwtProvider.class);
        when(jwt.generateAccessToken(type, 1L)).thenReturn("access");
        when(jwt.generateRefreshToken(type, 1L)).thenReturn("refresh");
        when(jwt.getRefreshTokenExpiration()).thenReturn(60_000L);
        var tokens = mock(TokenRepository.class);
        when(tokens.saveToken(eq("refresh"), eq(1L), any(Duration.class))).thenReturn(true);
        @SuppressWarnings("unchecked")
        ValueOperations<String, String> values = mock(ValueOperations.class);
        when(redis.opsForValue()).thenReturn(values);
        return new AuthenticationProtection(
                applicants,
                employers,
                passwords,
                states,
                totp,
                redis,
                jwt,
                tokens,
                Clock.systemUTC(),
                3,
                90,
                300,
                900);
    }

    @ParameterizedTest
    @CsvSource({"APPLICANT,false", "APPLICANT,true", "EMPLOYER,false", "EMPLOYER,true"})
    void wrongPasswordsDoNotLockAccountOrRevokeSessions(AccountType type, boolean enabled) {
        var protection = protection(type);
        if (enabled) {
            state.secret = "encrypted-secret";
            state.recoveryHashes = List.of("recovery-hash");
        }
        String stamp = protection.sessionStamp(type, 1L);

        for (int attempt = 0; attempt < 10; attempt++) {
            assertEquals(
                    401,
                    assertThrows(
                                    ApiFailure.class,
                                    () ->
                                            protection.login(
                                                    type, "account@example.test", "wrong-password"))
                            .getStatus());
        }

        assertFalse(state.locked);
        assertEquals(0, state.failures);
        assertEquals(0, state.lockedUntil);
        assertTrue(protection.sessionAllowed(type, 1L, stamp));
        if (enabled) {
            assertThrows(
                    AuthenticationChallenge.class,
                    () -> protection.login(type, "account@example.test", "correct-password"));
        } else {
            assertEquals(
                    "access",
                    protection
                            .login(type, "account@example.test", "correct-password")
                            .accessToken());
        }
    }

    @ParameterizedTest
    @EnumSource(AccountType.class)
    void wrongPasswordDoesNotChangeSecondFactorFailures(AccountType type) {
        var protection = protection(type);
        state.secret = "encrypted-secret";
        state.failures = 2;

        assertEquals(
                401,
                assertThrows(
                                ApiFailure.class,
                                () ->
                                        protection.login(
                                                type, "account@example.test", "wrong-password"))
                        .getStatus());
        assertFalse(state.locked);
        assertEquals(2, state.failures);
        assertThrows(
                AuthenticationChallenge.class,
                () -> protection.login(type, "account@example.test", "correct-password"));
        assertEquals(2, state.failures);
    }

    @ParameterizedTest
    @EnumSource(AccountType.class)
    void lockedAccountStillRequiresCorrectPasswordBeforeReportingLock(AccountType type) {
        var protection = protection(type);
        state.secret = "encrypted-secret";
        state.recoveryHashes = List.of("recovery-hash");
        state.locked = true;
        state.failures = 3;
        long lockedUntil = System.currentTimeMillis() + 900_000;
        state.lockedUntil = lockedUntil;

        assertEquals(
                401,
                assertThrows(
                                ApiFailure.class,
                                () ->
                                        protection.login(
                                                type, "account@example.test", "wrong-password"))
                        .getStatus());
        assertEquals(
                423,
                assertThrows(
                                ApiFailure.class,
                                () ->
                                        protection.login(
                                                type, "account@example.test", "correct-password"))
                        .getStatus());
        assertEquals(3, state.failures);
        assertEquals(lockedUntil, state.lockedUntil);
    }

    @ParameterizedTest
    @EnumSource(AccountType.class)
    void recoveryWithWrongPasswordDoesNotConsumeAccountRecoveryLimit(AccountType type) {
        var protection = protection(type);
        clearInvocations(redis);

        for (int attempt = 0; attempt < 12; attempt++) {
            assertEquals(
                    401,
                    assertThrows(
                                    ApiFailure.class,
                                    () ->
                                            protection.recover(
                                                    type,
                                                    "account@example.test",
                                                    "wrong-password",
                                                    "invalid-code",
                                                    "replacement-password",
                                                    false))
                            .getStatus());
        }

        verifyNoInteractions(redis);
        assertFalse(state.locked);
        assertEquals(0, state.failures);
    }

    @ParameterizedTest
    @EnumSource(AccountType.class)
    void repeatedTotpSubmissionsKeepReportingAccountLock(AccountType type) {
        var protection = protection(type);
        state.secret = totp.encrypt(totp.newSecret(), type.name() + ":1");
        state.recoveryHashes = List.of("recovery-hash");
        String stamp = protection.sessionStamp(type, 1L);
        var challenge =
                assertThrows(
                        AuthenticationChallenge.class,
                        () -> protection.login(type, "account@example.test", "correct-password"));
        String token = challenge.getChallengeToken();
        when(redis.opsForValue().get("auth:challenge:" + TotpSupport.digest(token)))
                .thenReturn(type.name() + ":1:" + stamp + ":TOTP");

        for (int attempt = 1; attempt <= 8; attempt++) {
            var error = assertThrows(ApiFailure.class, () -> protection.verify(token, "invalid"));
            assertEquals(attempt < 3 ? 401 : 423, error.getStatus());
            if (attempt >= 3) assertEquals("ACCOUNT_LOCKED", error.getCode());
        }

        assertEquals(3, state.failures);
        assertEquals(1, state.sessionVersion);
        assertFalse(protection.sessionAllowed(type, 1L, stamp));
    }

    @ParameterizedTest
    @EnumSource(AccountType.class)
    void invalidChallengesDoNotExposeLockOrAuthorizeRevokedSession(AccountType type) {
        var protection = protection(type);
        String stamp = protection.sessionStamp(type, 1L);
        state.locked = true;
        state.secret = "encrypted-secret";
        state.recoveryHashes = List.of("recovery-hash");
        state.sessionVersion++;
        var values = redis.opsForValue();
        when(values.get("auth:challenge:" + TotpSupport.digest("wrong-purpose")))
                .thenReturn(type.name() + ":1:" + stamp + ":PASSWORD_CHANGE");
        when(values.get("auth:challenge:" + TotpSupport.digest("revoked")))
                .thenReturn(type.name() + ":1:" + stamp + ":TOTP");

        for (String token : List.of("unknown", "expired", "wrong-purpose")) {
            assertEquals(
                    401,
                    assertThrows(ApiFailure.class, () -> protection.verify(token, "123456"))
                            .getStatus());
        }
        state.locked = false;
        assertEquals(
                401,
                assertThrows(ApiFailure.class, () -> protection.verify("revoked", "123456"))
                        .getStatus());
    }
}
