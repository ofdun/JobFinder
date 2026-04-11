package com.ofdun.jobfinder.features.auth.data.jwt.repository;

import static org.junit.jupiter.api.Assertions.*;

import java.time.Duration;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.redis.test.autoconfigure.DataRedisTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.context.annotation.Primary;
import org.springframework.data.redis.connection.RedisStandaloneConfiguration;
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.utility.DockerImageName;

@ActiveProfiles("test")
@DataRedisTest
@Import(RedisTokenRepository.class)
@Testcontainers
class RedisTokenRepositoryIT {

    @Container
    static final GenericContainer<?> redis =
            new GenericContainer<>(DockerImageName.parse("redis:7-alpine")).withExposedPorts(6379);

    @DynamicPropertySource
    static void registerRedis(DynamicPropertyRegistry registry) {
        registry.add("spring.redis.host", redis::getHost);
        registry.add("spring.redis.port", () -> redis.getMappedPort(6379));
    }

    @TestConfiguration
    static class TestRedisConfig {
        @Bean
        @Primary
        LettuceConnectionFactory redisConnectionFactory() {
            RedisStandaloneConfiguration cfg =
                    new RedisStandaloneConfiguration(redis.getHost(), redis.getMappedPort(6379));
            return new LettuceConnectionFactory(cfg);
        }

        @Bean
        StringRedisTemplate stringRedisTemplate(LettuceConnectionFactory connectionFactory) {
            return new StringRedisTemplate(connectionFactory);
        }
    }

    @Autowired private RedisTokenRepository tokenRepository;

    @Autowired private StringRedisTemplate redisTemplate;

    @BeforeEach
    void cleanup() {
        try {
            redisTemplate.getConnectionFactory().getConnection().flushAll();
        } catch (Exception ignored) {
        }
    }

    @Test
    void saveToken_withExpiry_thenExpires() throws InterruptedException {
        String token = "save-expiry";
        Long userId = 2L;

        Boolean saved = tokenRepository.saveToken(token, userId, Duration.ofMillis(200));

        assertTrue(saved);
        String raw = redisTemplate.opsForValue().get(token);
        assertEquals(String.valueOf(userId), raw);

        Thread.sleep(500);

        String after = redisTemplate.opsForValue().get(token);
        assertNull(after);
    }

    @Test
    void getUserIdByToken_whenExists_thenReturnLong() {
        String token = "get-exists";
        Long userId = 3L;
        redisTemplate.opsForValue().set(token, String.valueOf(userId));

        Long result = tokenRepository.getUserIdByToken(token);

        assertNotNull(result);
        assertEquals(userId, result);
    }

    @Test
    void getUserIdByToken_whenMissing_thenNullReturned() {
        Long result = tokenRepository.getUserIdByToken("missing-token");
        assertNull(result);
    }

    @Test
    void getUserIdByToken_whenMalformedValue_thenNullReturned() {
        String token = "malformed";
        redisTemplate.opsForValue().set(token, "not-a-number");

        Long result = tokenRepository.getUserIdByToken(token);

        assertNull(result);
    }

    @Test
    void deleteToken_whenExists_thenTrueAndRemoved() {
        String token = "del-exists";
        Long userId = 4L;
        redisTemplate.opsForValue().set(token, String.valueOf(userId));

        Boolean deleted = tokenRepository.deleteToken(token);

        assertTrue(deleted);
        assertNull(redisTemplate.opsForValue().get(token));
    }
}
