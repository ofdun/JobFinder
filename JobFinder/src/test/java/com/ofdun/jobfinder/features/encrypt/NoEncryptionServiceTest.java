package com.ofdun.jobfinder.features.encrypt;

import static com.ofdun.jobfinder.support.TestDataMother.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.util.*;
import org.junit.jupiter.api.*;
import org.mockito.*;

class NoEncryptionServiceTest {

    @Test
    @Tag("equivalence")
    void encrypt_whenPresent_preservesContent() {
        var service = new NoEncryptionService();

        var result = service.encrypt("text");

        assertEquals("text", result);
    }

    @Test
    @Tag("equivalence")
    void encrypt_whenNull_preservesNull() {
        var service = new NoEncryptionService();

        var result = service.encrypt(null);

        assertNull(result);
    }

    @Test
    @Tag("equivalence")
    void matches_whenEqual_returnsTrue() {
        var service = new NoEncryptionService();

        var result = service.matches("text", "text");

        assertTrue(result);
    }

    @Test
    @Tag("equivalence")
    void matches_whenDifferent_returnsFalse() {
        var service = new NoEncryptionService();

        var result = service.matches("text", "other");

        assertFalse(result);
    }
}
