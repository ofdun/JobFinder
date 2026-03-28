package com.ofdun.jobfinder.features.auth.data.jwt.repository;

import com.ofdun.jobfinder.features.auth.domain.repository.TokenRepository;
import org.springframework.stereotype.Component;

import java.time.Duration;

@Component
public class RedisTokenRepository implements TokenRepository {
    @Override
    public Boolean saveToken(String token, Long userId, Duration expiryTime) {
        return false;
    }

    @Override
    public Long getUserIdByToken(String token) {
        return 0L;
    }

    @Override
    public Boolean deleteToken(String token) {
        return false;
    }
}
