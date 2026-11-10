package com.ofdun.jobfinder.features.auth.mfa;

import com.ofdun.jobfinder.features.api.ApiFailure;

import dev.samstevens.totp.code.DefaultCodeGenerator;
import dev.samstevens.totp.code.HashingAlgorithm;
import dev.samstevens.totp.qr.QrData;
import dev.samstevens.totp.qr.ZxingPngQrGenerator;
import dev.samstevens.totp.secret.DefaultSecretGenerator;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.SecureRandom;
import java.util.Arrays;
import java.util.Base64;

import javax.crypto.Cipher;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.SecretKeySpec;

@Component
public class TotpSupport {
    private final String encryptionKey;
    private final SecureRandom random = new SecureRandom();

    public TotpSupport(@Value("${app.auth.encryption-key:}") String encryptionKey) {
        this.encryptionKey = encryptionKey;
    }

    public String newSecret() {
        return new DefaultSecretGenerator().generate();
    }

    public String randomToken() {
        byte[] bytes = new byte[32];
        random.nextBytes(bytes);
        return Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
    }

    public String qr(String secret, String email) {
        try {
            var data =
                    new QrData.Builder()
                            .label(email)
                            .issuer("JobFinder")
                            .secret(secret)
                            .algorithm(HashingAlgorithm.SHA1)
                            .digits(6)
                            .period(30)
                            .build();
            return "data:image/png;base64,"
                    + Base64.getEncoder().encodeToString(new ZxingPngQrGenerator().generate(data));
        } catch (Exception ex) {
            throw new IllegalStateException("Cannot create authenticator QR code");
        }
    }

    public long matchedStep(String secret, String code, long nowMillis, long lastStep) {
        if (code == null || !code.matches("[0-9]{6}")) return -1;
        long current = nowMillis / 30_000;
        try {
            var generator = new DefaultCodeGenerator();
            for (long step = current - 1; step <= current + 1; step++) {
                if (step > lastStep
                        && MessageDigest.isEqual(
                                generator
                                        .generate(secret, step)
                                        .getBytes(StandardCharsets.US_ASCII),
                                code.getBytes(StandardCharsets.US_ASCII))) return step;
            }
            return -1;
        } catch (Exception ex) {
            throw new IllegalStateException("Cannot verify authenticator code");
        }
    }

    public String encrypt(String secret, String accountKey) {
        try {
            var key = key();
            byte[] nonce = new byte[12];
            random.nextBytes(nonce);
            var cipher = Cipher.getInstance("AES/GCM/NoPadding");
            cipher.init(Cipher.ENCRYPT_MODE, key, new GCMParameterSpec(128, nonce));
            cipher.updateAAD(accountKey.getBytes(StandardCharsets.UTF_8));
            byte[] encrypted = cipher.doFinal(secret.getBytes(StandardCharsets.UTF_8));
            byte[] result = Arrays.copyOf(nonce, nonce.length + encrypted.length);
            System.arraycopy(encrypted, 0, result, nonce.length, encrypted.length);
            return Base64.getEncoder().encodeToString(result);
        } catch (ApiFailure ex) {
            throw ex;
        } catch (Exception ex) {
            throw new IllegalStateException("Cannot protect authenticator secret");
        }
    }

    public String decrypt(String encrypted, String accountKey) {
        try {
            var cipher = Cipher.getInstance("AES/GCM/NoPadding");
            byte[] data = Base64.getDecoder().decode(encrypted);
            cipher.init(
                    Cipher.DECRYPT_MODE, key(), new GCMParameterSpec(128, Arrays.copyOf(data, 12)));
            cipher.updateAAD(accountKey.getBytes(StandardCharsets.UTF_8));
            return new String(cipher.doFinal(data, 12, data.length - 12), StandardCharsets.UTF_8);
        } catch (ApiFailure ex) {
            throw ex;
        } catch (Exception ex) {
            throw new IllegalStateException("Cannot read authenticator secret");
        }
    }

    private SecretKeySpec key() {
        try {
            byte[] bytes = Base64.getDecoder().decode(encryptionKey);
            if (bytes.length == 32) return new SecretKeySpec(bytes, "AES");
        } catch (IllegalArgumentException ignored) {
        }
        throw new ApiFailure(
                "MFA_UNAVAILABLE", 503, "Не настроен ключ защиты двухфакторной аутентификации");
    }

    public static String digest(String value) {
        try {
            return Base64.getUrlEncoder()
                    .withoutPadding()
                    .encodeToString(
                            MessageDigest.getInstance("SHA-256")
                                    .digest(value.getBytes(StandardCharsets.UTF_8)));
        } catch (Exception ex) {
            throw new IllegalStateException(ex);
        }
    }
}
