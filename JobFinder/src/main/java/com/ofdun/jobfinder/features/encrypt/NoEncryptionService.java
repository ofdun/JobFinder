package com.ofdun.jobfinder.features.encrypt;

@Deprecated
public class NoEncryptionService implements EncryptionService {
    @Override
    public String encrypt(String content) {
        return content;
    }

    @Override
    public Boolean matches(String rawContent, String encrypted) {
        return rawContent.equals(encrypted);
    }
}
