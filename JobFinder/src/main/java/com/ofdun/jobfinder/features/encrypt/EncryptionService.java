package com.ofdun.jobfinder.features.encrypt;

public interface EncryptionService {
    String encrypt(String content);

    Boolean matches(String rawContent, String encrypted);
}
