package com.ofdun.jobfinder.features.auth.data.jwt.impl;

import com.ofdun.jobfinder.features.auth.domain.jwt.JwtProvider;
import com.ofdun.jobfinder.features.auth.enums.AccountType;
import com.ofdun.jobfinder.features.auth.mfa.AccountSecurity;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import io.micrometer.observation.annotation.Observed;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import javax.crypto.SecretKey;
import lombok.NonNull;

public class BasicJwtProvider implements JwtProvider {
    private final AccountSecurity accounts;

    private final SecretKey secret;
    private final Long accessExpirationTimeMs;
    private final Long refreshExpirationTimeMs;

    public BasicJwtProvider(
            String secret,
            Long accessExpirationTimeMs,
            Long refreshExpirationTimeMs,
            AccountSecurity accounts) {
        this.accounts = accounts;
        this.secret = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
        this.accessExpirationTimeMs = accessExpirationTimeMs;
        this.refreshExpirationTimeMs = refreshExpirationTimeMs;
    }

    @Override
    public Long getRefreshTokenExpiration() {
        return refreshExpirationTimeMs;
    }

    @Override
    @Observed(
            name = "jobfinder.auth.service.generateAccessToken",
            contextualName = "Выпуск access-токена",
            lowCardinalityKeyValues = {"feature", "auth", "layer", "service"})
    public String generateAccessToken(AccountType type, Long userId) {
        return Jwts.builder()
                .subject(userId.toString())
                .claim("role", type.name())
                .claim("token_use", "access")
                .claim("auth_stamp", accounts.sessionStamp(type, userId))
                .id(java.util.UUID.randomUUID().toString())
                .issuedAt(new Date())
                .expiration(new Date(System.currentTimeMillis() + this.accessExpirationTimeMs))
                .signWith(secret)
                .compact();
    }

    @Override
    @Observed(
            name = "jobfinder.auth.service.generateRefreshToken",
            contextualName = "Выпуск refresh-токена",
            lowCardinalityKeyValues = {"feature", "auth", "layer", "service"})
    public String generateRefreshToken(AccountType type, Long userId) {
        return Jwts.builder()
                .subject(userId.toString())
                .claim("role", type.name())
                .claim("token_use", "refresh")
                .claim("auth_stamp", accounts.sessionStamp(type, userId))
                .id(java.util.UUID.randomUUID().toString())
                .issuedAt(new Date())
                .expiration(new Date(System.currentTimeMillis() + this.refreshExpirationTimeMs))
                .signWith(secret)
                .compact();
    }

    @Override
    @Observed(
            name = "jobfinder.auth.service.validateToken",
            contextualName = "Проверка подписи JWT",
            lowCardinalityKeyValues = {"feature", "auth", "layer", "service"})
    public Boolean validateToken(@NonNull String token, AccountType type) {
        try {
            var claims =
                    Jwts.parser().verifyWith(secret).build().parseSignedClaims(token).getPayload();
            return getAccountType(token) == type
                    && accounts.sessionAllowed(
                            type, getUserId(token), claims.get("auth_stamp", String.class));
        } catch (Exception e) {
            return false;
        }
    }

    @Override
    @Observed(
            name = "jobfinder.auth.service.validateAccessToken",
            contextualName = "Проверка access-токена",
            lowCardinalityKeyValues = {"feature", "auth", "layer", "service"})
    public Boolean validateAccessToken(String token, AccountType type) {
        return validatePurpose(token, type, "access", false);
    }

    @Override
    @Observed(
            name = "jobfinder.auth.service.validateRefreshToken",
            contextualName = "Проверка refresh-токена",
            lowCardinalityKeyValues = {"feature", "auth", "layer", "service"})
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
    @Observed(
            name = "jobfinder.auth.service.getUserId",
            contextualName = "Чтение идентификатора из JWT",
            lowCardinalityKeyValues = {"feature", "auth", "layer", "service"})
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
    @Observed(
            name = "jobfinder.auth.service.getAccountType",
            contextualName = "Чтение роли из JWT",
            lowCardinalityKeyValues = {"feature", "auth", "layer", "service"})
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
