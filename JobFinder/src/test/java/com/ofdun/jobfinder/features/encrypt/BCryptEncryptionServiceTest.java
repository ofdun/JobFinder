package com.ofdun.jobfinder.features.encrypt;

import static com.ofdun.jobfinder.support.TestDataMother.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.util.*;
import org.junit.jupiter.api.*;
import org.mockito.*;

class BCryptEncryptionServiceTest {

    @Test
    @Tag("equivalence")
    void encrypt_whenValid_producesVerifiableHash() {
        var service = new BCryptEncryptionService();

        var result = service.encrypt("secret");

        assertNotEquals("secret", result);
        assertTrue(org.springframework.security.crypto.bcrypt.BCrypt.checkpw("secret", result));
    }

    @Test
    @Tag("equivalence")
    void encrypt_whenNull_rejectsPassword() {
        var service = new BCryptEncryptionService();

        var error = assertThrows(NullPointerException.class, () -> service.encrypt(null));

        assertNotNull(error);
    }

    @Test
    @Tag("equivalence")
    void matches_whenCorrectPassword_returnsTrue() {
        var service = new BCryptEncryptionService();
        var hash =
                org.springframework.security.crypto.bcrypt.BCrypt.hashpw(
                        "secret", org.springframework.security.crypto.bcrypt.BCrypt.gensalt(4));

        var result = service.matches("secret", hash);

        assertTrue(result);
    }

    @Test
    @Tag("equivalence")
    void matches_whenWrongPassword_returnsFalse() {
        var service = new BCryptEncryptionService();
        var hash =
                org.springframework.security.crypto.bcrypt.BCrypt.hashpw(
                        "secret", org.springframework.security.crypto.bcrypt.BCrypt.gensalt(4));

        var result = service.matches("wrong", hash);

        assertFalse(result);
    }
}
