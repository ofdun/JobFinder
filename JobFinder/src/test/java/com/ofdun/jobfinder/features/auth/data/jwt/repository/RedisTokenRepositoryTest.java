package com.ofdun.jobfinder.features.auth.data.jwt.repository;

import static com.ofdun.jobfinder.support.TestDataMother.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.util.*;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;

@ExtendWith(MockitoExtension.class)
class RedisTokenRepositoryTest {
    @Mock private RedisTemplate<String, String> redis;
    @Mock private ValueOperations<String, String> values;
    @InjectMocks private RedisTokenRepository repository;

    @Test
    @Tag("equivalence")
    void saveToken_whenAvailable_storesIdAndTtl() {
        when(redis.opsForValue()).thenReturn(values);
        var ttl = java.time.Duration.ofMinutes(5);

        var result = repository.saveToken("token", 42L, ttl);

        assertTrue(result);
        verify(values).set("token", "42", ttl);
    }

    @Test
    @Tag("equivalence")
    void saveToken_whenUnavailable_returnsFalse() {
        when(redis.opsForValue()).thenThrow(new IllegalStateException("offline"));

        var result = repository.saveToken("token", 42L, java.time.Duration.ofMinutes(5));

        assertFalse(result);
    }

    @Test
    @Tag("equivalence")
    void getUserIdByToken_whenPresent_parsesId() {
        when(redis.opsForValue()).thenReturn(values);
        when(values.get("token")).thenReturn("42");

        var result = repository.getUserIdByToken("token");

        assertEquals(42L, result);
    }

    @org.junit.jupiter.params.ParameterizedTest
    @Tag("boundary")
    @Tag("equivalence")
    @org.junit.jupiter.params.provider.NullSource
    @org.junit.jupiter.params.provider.ValueSource(strings = {"", "invalid", "9223372036854775808"})
    void getUserIdByToken_whenMissingOrMalformed_returnsNull(String value) {
        when(redis.opsForValue()).thenReturn(values);
        when(values.get("token")).thenReturn(value);

        var result = repository.getUserIdByToken("token");

        assertNull(result);
    }

    @Test
    @Tag("equivalence")
    void deleteToken_whenAvailable_removesToken() {
        when(redis.delete("token")).thenReturn(true);

        var result = repository.deleteToken("token");

        assertTrue(result);
        verify(redis).delete("token");
    }

    @Test
    @Tag("equivalence")
    void deleteToken_whenUnavailable_returnsFalse() {
        when(redis.delete("token")).thenThrow(new IllegalStateException("offline"));

        var result = repository.deleteToken("token");

        assertFalse(result);
    }
}
