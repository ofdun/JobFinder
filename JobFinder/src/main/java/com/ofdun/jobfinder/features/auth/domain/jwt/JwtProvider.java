package com.ofdun.jobfinder.features.auth.domain.jwt;

import com.ofdun.jobfinder.shared.auth.domain.enums.AccountType;
import lombok.NonNull;

public interface JwtProvider {
    String generateAccessToken(AccountType type, Long userId);
    String generateRefreshToken(AccountType type, Long userId);
    Boolean validateToken(@NonNull String token, AccountType type);
    Long getRefreshTokenExpiration();
}
