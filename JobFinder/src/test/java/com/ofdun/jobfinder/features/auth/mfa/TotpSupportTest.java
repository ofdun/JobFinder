package com.ofdun.jobfinder.features.auth.mfa;

import static org.junit.jupiter.api.Assertions.*;

import com.ofdun.jobfinder.features.api.ApiFailure;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import org.apache.commons.codec.binary.Base32;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;

class TotpSupportTest {
    private static final String RFC_6238_SECRET =
            new Base32().encodeToString("12345678901234567890".getBytes(StandardCharsets.US_ASCII));
    private final TotpSupport totp =
            new TotpSupport(Base64.getEncoder().encodeToString(new byte[32]));

    @ParameterizedTest
    @CsvSource({
        "59,287082",
        "1111111109,081804",
        "1111111111,050471",
        "1234567890,005924",
        "2000000000,279037",
        "20000000000,353130"
    })
    void matchesRfc6238Vectors(long seconds, String code) {
        assertEquals(seconds / 30, totp.matchedStep(RFC_6238_SECRET, code, seconds * 1000, -1));
    }

    @Test
    void rejectsReplayAndOutOfWindowCodes() {
        String secret = RFC_6238_SECRET;
        assertEquals(-1, totp.matchedStep(secret, "287082", 59_000, 1));
        assertEquals(-1, totp.matchedStep(secret, "287082", 150_000, -1));
        assertEquals(1, totp.matchedStep(secret, "287082", 60_000, -1));
    }

    @ParameterizedTest
    @NullAndEmptySource
    @ValueSource(strings = {"12345", "1234567", "abcdef", "１２３４５６"})
    void rejectsMalformedCode(String code) {
        assertEquals(-1, totp.matchedStep(totp.newSecret(), code, System.currentTimeMillis(), -1));
    }

    @Test
    void encryptionIsRandomizedAndBoundToAccount() {
        String secret = totp.newSecret();
        String first = totp.encrypt(secret, "APPLICANT:1");
        String second = totp.encrypt(secret, "APPLICANT:1");
        assertFalse(first.equals(second));
        assertTrue(secret.equals(totp.decrypt(first, "APPLICANT:1")));
        assertThrows(IllegalStateException.class, () -> totp.decrypt(first, "EMPLOYER:1"));
        byte[] damaged = Base64.getDecoder().decode(first);
        damaged[damaged.length - 1] ^= 1;
        assertThrows(
                IllegalStateException.class,
                () -> totp.decrypt(Base64.getEncoder().encodeToString(damaged), "APPLICANT:1"));
    }

    @ParameterizedTest
    @ValueSource(strings = {"", "not-base64", "YWJj"})
    void refusesEnrollmentWithoutStrongEncryptionKey(String key) {
        assertEquals(
                503,
                assertThrows(
                                ApiFailure.class,
                                () -> new TotpSupport(key).encrypt("secret", "APPLICANT:1"))
                        .getStatus());
    }

    @Test
    void producesQrPngAndUniqueRecoveryTokens() {
        assertTrue(
                totp.qr(totp.newSecret(), "test@example.com")
                        .startsWith("data:image/png;base64,iVBOR"));
        assertFalse(totp.randomToken().equals(totp.randomToken()));
    }
}
