package com.ofdun.jobfinder.features.auth.domain.jwt;

import com.ofdun.jobfinder.features.auth.enums.AccountType;
import lombok.NonNull;

public interface JwtProvider {
    String generateAccessToken(AccountType type, Long userId);

    String generateRefreshToken(AccountType type, Long userId);

    Boolean validateToken(@NonNull String token, AccountType type);

    Long getRefreshTokenExpiration();

    /**
     * Извлекает userId (subject) из JWT. Должно работать как для access, так и для refresh токенов.
     */
    Long getUserId(@NonNull String token);

    /**
     * Извлекает тип аккаунта из JWT (claim role/type).
     */
    AccountType getAccountType(@NonNull String token);
}
