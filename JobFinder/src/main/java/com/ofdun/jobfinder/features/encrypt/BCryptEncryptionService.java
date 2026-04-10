package com.ofdun.jobfinder.features.encrypt;

import org.springframework.security.crypto.bcrypt.BCrypt;
import org.springframework.stereotype.Service;

@Service
public class BCryptEncryptionService implements EncryptionService {
    @Override
    public String encrypt(String content) {
        return BCrypt.hashpw(content, BCrypt.gensalt());
    }

    @Override
    public Boolean matches(String rawContent, String encrypted) {
        return BCrypt.checkpw(rawContent, encrypted);
    }
}
