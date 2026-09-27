package com.ofdun.jobfinder.features.auth.data.jwt.impl;

import static com.ofdun.jobfinder.support.TestDataMother.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import com.ofdun.jobfinder.features.auth.enums.AccountType;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import java.util.*;
import org.junit.jupiter.api.*;
import org.mockito.*;

class BasicJwtProviderTest {
    private static final String SECRET = "0123456789abcdef0123456789abcdef";

    private static javax.crypto.SecretKey key() {
        return Keys.hmacShaKeyFor(SECRET.getBytes(java.nio.charset.StandardCharsets.UTF_8));
    }

    private static BasicJwtProvider provider() {
        return new BasicJwtProvider(SECRET, 60000L, 300000L);
    }

    private static String token(String subject, String role, Date expiration) {
        return Jwts.builder()
                .subject(subject)
                .claim("role", role)
                .expiration(expiration)
                .signWith(key())
                .compact();
    }

    @Test
    @Tag("equivalence")
    void generateAccessToken_whenValid_containsSignedClaims() {
        var provider = provider();

        var token = provider.generateAccessToken(AccountType.APPLICANT, 42L);

        var claims = Jwts.parser().verifyWith(key()).build().parseSignedClaims(token).getPayload();
        assertEquals("42", claims.getSubject());
        assertEquals("APPLICANT", claims.get("role"));
        assertEquals(60000L, claims.getExpiration().getTime() - claims.getIssuedAt().getTime());
    }

    @Test
    @Tag("equivalence")
    void generateAccessToken_whenUserIdNull_rejectsRequest() {
        var provider = provider();

        var error =
                assertThrows(
                        NullPointerException.class,
                        () -> provider.generateAccessToken(AccountType.APPLICANT, null));

        assertNotNull(error);
    }

    @Test
    @Tag("equivalence")
    void generateRefreshToken_whenValid_containsSignedClaims() {
        var provider = provider();

        var token = provider.generateRefreshToken(AccountType.APPLICANT, 42L);

        var claims = Jwts.parser().verifyWith(key()).build().parseSignedClaims(token).getPayload();
        assertEquals("42", claims.getSubject());
        assertEquals("APPLICANT", claims.get("role"));
        assertEquals(300000L, claims.getExpiration().getTime() - claims.getIssuedAt().getTime());
    }

    @Test
    @Tag("equivalence")
    void generateRefreshToken_whenUserIdNull_rejectsRequest() {
        var provider = provider();

        var error =
                assertThrows(
                        NullPointerException.class,
                        () -> provider.generateRefreshToken(AccountType.APPLICANT, null));

        assertNotNull(error);
    }

    @Test
    @Tag("equivalence")
    void getRefreshTokenExpiration_returnsConfiguredDuration() {
        var provider = provider();

        var result = provider.getRefreshTokenExpiration();

        assertEquals(300000L, result);
    }

    @Test
    @Tag("equivalence")
    void getRefreshTokenExpiration_preservesZeroBoundary() {
        var provider = new BasicJwtProvider(SECRET, 60000L, 0L);

        var result = provider.getRefreshTokenExpiration();

        assertEquals(0L, result);
    }

    @Test
    @Tag("equivalence")
    void validateToken_whenSignedForRole_returnsTrue() {
        var provider = provider();
        var token = token("42", "APPLICANT", new Date(System.currentTimeMillis() + 3600000));

        var result = provider.validateToken(token, AccountType.APPLICANT);

        assertTrue(result);
    }

    @Test
    @Tag("equivalence")
    void validateToken_whenWrongRole_returnsFalse() {
        var provider = provider();
        var token = token("42", "EMPLOYER", new Date(System.currentTimeMillis() + 3600000));

        var result = provider.validateToken(token, AccountType.APPLICANT);

        assertFalse(result);
    }

    @Test
    @Tag("equivalence")
    void validateToken_whenExpired_returnsFalse() {
        var provider = provider();
        var token = token("42", "APPLICANT", new Date(0));

        var result = provider.validateToken(token, AccountType.APPLICANT);

        assertFalse(result);
    }

    @Test
    @Tag("equivalence")
    void getUserId_whenSigned_extractsSubject() {
        var provider = provider();
        var token = token("42", "APPLICANT", new Date(System.currentTimeMillis() + 3600000));

        var result = provider.getUserId(token);

        assertEquals(42L, result);
    }

    @Test
    @Tag("equivalence")
    void getUserId_whenMalformedSubject_rejectsToken() {
        var provider = provider();
        var token =
                token("not-a-number", "APPLICANT", new Date(System.currentTimeMillis() + 3600000));

        var error = assertThrows(NumberFormatException.class, () -> provider.getUserId(token));

        assertNotNull(error);
    }

    @Test
    @Tag("equivalence")
    void getAccountType_whenSigned_extractsRole() {
        var provider = provider();
        var token = token("42", "EMPLOYER", new Date(System.currentTimeMillis() + 3600000));

        var result = provider.getAccountType(token);

        assertEquals(AccountType.EMPLOYER, result);
    }

    @Test
    @Tag("equivalence")
    void getAccountType_whenRoleMissing_rejectsToken() {
        var provider = provider();
        var token = token("42", null, new Date(System.currentTimeMillis() + 3600000));

        var error =
                assertThrows(IllegalArgumentException.class, () -> provider.getAccountType(token));

        assertEquals("JWT role/type claim is missing", error.getMessage());
    }
}
