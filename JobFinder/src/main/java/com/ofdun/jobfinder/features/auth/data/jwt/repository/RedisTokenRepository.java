package com.ofdun.jobfinder.features.auth.data.jwt.repository;

import com.ofdun.jobfinder.features.auth.domain.repository.TokenRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

import java.time.Duration;

@Component
@RequiredArgsConstructor
public class RedisTokenRepository implements TokenRepository {
    private final RedisTemplate<String, String> redisTemplate;

    @Override
    public Boolean saveToken(String token, Long userId, Duration expiryTime) {
        try {
            redisTemplate.opsForValue().set(token, userId.toString(), expiryTime);
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    @Override
    public Long getUserIdByToken(String token) {
        String userIdStr = redisTemplate.opsForValue().get(token);
        if (userIdStr != null) {
            try {
                return Long.parseLong(userIdStr);
            } catch (NumberFormatException e) {
                return null;
            }
        }
        return null;
    }

    @Override
    public Boolean deleteToken(String token) {
        try {
            redisTemplate.delete(token);
            return true;
        } catch (Exception e) {
            return false;
        }
    }
}
