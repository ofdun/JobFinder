package com.ofdun.jobfinder.features.auth.data.jwt.repository;

import com.ofdun.jobfinder.features.auth.domain.repository.TokenRepository;
import io.micrometer.observation.annotation.Observed;
import java.time.Duration;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class RedisTokenRepository implements TokenRepository {
    private final RedisTemplate<String, String> redisTemplate;

    @Override
    @Observed(
            name = "jobfinder.auth.repository.saveToken",
            contextualName = "Redis: сохранение refresh-токена",
            lowCardinalityKeyValues = {
                "feature",
                "auth",
                "layer",
                "repository",
                "storage",
                "Redis"
            })
    public Boolean saveToken(String token, Long userId, Duration expiryTime) {
        try {
            redisTemplate.opsForValue().set(token, userId.toString(), expiryTime);
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    @Override
    @Observed(
            name = "jobfinder.auth.repository.getUserIdByToken",
            contextualName = "Redis: поиск владельца refresh-токена",
            lowCardinalityKeyValues = {
                "feature",
                "auth",
                "layer",
                "repository",
                "storage",
                "Redis"
            })
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
    @Observed(
            name = "jobfinder.auth.repository.deleteToken",
            contextualName = "Redis: отзыв refresh-токена",
            lowCardinalityKeyValues = {
                "feature",
                "auth",
                "layer",
                "repository",
                "storage",
                "Redis"
            })
    public Boolean deleteToken(String token) {
        try {
            redisTemplate.delete(token);
            return true;
        } catch (Exception e) {
            return false;
        }
    }
}
