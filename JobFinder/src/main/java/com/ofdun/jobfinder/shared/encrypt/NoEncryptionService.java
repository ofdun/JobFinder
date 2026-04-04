package com.ofdun.jobfinder.shared.encrypt;

import org.springframework.stereotype.Service;

@Service
public class NoEncryptionService implements EncryptionService {
    @Override
    public String encrypt(String content) {
        return content;
    }
}
