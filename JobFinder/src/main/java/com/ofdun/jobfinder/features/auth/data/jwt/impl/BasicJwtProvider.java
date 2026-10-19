package com.ofdun.jobfinder.features.auth.data.jwt.impl;

import com.ofdun.jobfinder.features.auth.domain.jwt.JwtProvider;
import com.ofdun.jobfinder.features.auth.enums.AccountType;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import javax.crypto.SecretKey;
import lombok.NonNull;

public class BasicJwtProvider implements JwtProvider {
    private final SecretKey secret;
    private final Long accessExpirationTimeMs;
    private final Long refreshExpirationTimeMs;

    public BasicJwtProvider(
            String secret, Long accessExpirationTimeMs, Long refreshExpirationTimeMs) {
        this.secret = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
        this.accessExpirationTimeMs = accessExpirationTimeMs;
        this.refreshExpirationTimeMs = refreshExpirationTimeMs;
    }

    @Override
    public Long getRefreshTokenExpiration() {
        return refreshExpirationTimeMs;
    }

    @Override
    public String generateAccessToken(AccountType type, Long userId) {
        return Jwts.builder()
                .subject(userId.toString())
                .claim("role", type.name())
                .claim("token_use", "access")
                .id(java.util.UUID.randomUUID().toString())
                .issuedAt(new Date())
                .expiration(new Date(System.currentTimeMillis() + this.accessExpirationTimeMs))
                .signWith(secret)
                .compact();
    }

    @Override
    public String generateRefreshToken(AccountType type, Long userId) {
        return Jwts.builder()
                .subject(userId.toString())
                .claim("role", type.name())
                .claim("token_use", "refresh")
                .id(java.util.UUID.randomUUID().toString())
                .issuedAt(new Date())
                .expiration(new Date(System.currentTimeMillis() + this.refreshExpirationTimeMs))
                .signWith(secret)
                .compact();
    }

    @Override
    public Boolean validateToken(@NonNull String token, AccountType type) {
        try {
            return getAccountType(token) == type;
        } catch (Exception e) {
            return false;
        }
    }

    @Override
    public Boolean validateAccessToken(String token, AccountType type) {
        return validatePurpose(token, type, "access", false);
    }

    @Override
    public Boolean validateRefreshToken(String token, AccountType type) {
        // Legacy refresh tokens are additionally checked against their Redis record.
        return validatePurpose(token, type, "refresh", true);
    }

    private boolean validatePurpose(
            String token, AccountType type, String purpose, boolean allowLegacy) {
        try {
            if (!validateToken(token, type)) return false;
            var claims =
                    Jwts.parser().verifyWith(secret).build().parseSignedClaims(token).getPayload();
            var use = claims.get("token_use", String.class);
            return purpose.equals(use) || (allowLegacy && use == null);
        } catch (RuntimeException ex) {
            return false;
        }
    }

    @Override
    public Long getUserId(@NonNull String token) {
        Claims claims =
                Jwts.parser().verifyWith(secret).build().parseSignedClaims(token).getPayload();
        String sub = claims.getSubject();
        if (sub == null) {
            throw new IllegalArgumentException("JWT subject is null");
        }
        return Long.parseLong(sub);
    }

    @Override
    public AccountType getAccountType(@NonNull String token) {
        Claims claims =
                Jwts.parser().verifyWith(secret).build().parseSignedClaims(token).getPayload();

        String role = claims.get("role", String.class);
        if (role == null || role.isBlank()) {
            throw new IllegalArgumentException("JWT role/type claim is missing");
        }
        return AccountType.valueOf(role);
    }
}
