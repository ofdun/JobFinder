package com.ofdun.jobfinder.features.auth.domain.repository;

import java.time.Duration;

public interface TokenRepository {
    Boolean saveToken(String token, Long userId, Duration expiryTime);
    Long getUserIdByToken(String token);
    Boolean deleteToken(String token);
}
