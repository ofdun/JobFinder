package com.ofdun.jobfinder.features.auth.data.jwt.impl;

import com.ofdun.jobfinder.features.auth.domain.jwt.JwtProvider;
import com.ofdun.jobfinder.shared.auth.domain.enums.AccountType;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import lombok.NonNull;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;

public class BasicJwtProvider implements JwtProvider {
    private final SecretKey secret;
    private final Long accessExpirationTimeMs;
    private final Long refreshExpirationTimeMs;

    public BasicJwtProvider(String secret,
                            Long accessExpirationTimeMs,
                            Long refreshExpirationTimeMs) {
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
                .issuedAt(new Date())
                .expiration(new Date(System.currentTimeMillis() + this.accessExpirationTimeMs))
                .signWith(secret)
                .compact();
    }

    @Override
    public String generateRefreshToken(AccountType type, Long userId) {
        return Jwts.builder()
                .subject(userId.toString())
                .claim("role", type)
                .claim("type", type.name())
                .issuedAt(new Date())
                .expiration(new Date(System.currentTimeMillis() + this.refreshExpirationTimeMs))
                .signWith(secret)
                .compact();
    }

    @Override
    public Boolean validateToken(@NonNull String token, AccountType type) {
        try {
            var claims = Jwts.parser()
                    .verifyWith(secret)
                    .build()
                    .parseSignedClaims(token)
                    .getPayload();

            String role = claims.get("role", String.class);
            return role != null && role.equals(type.name());
        } catch (Exception e) {
            return false;
        }
    }
}
