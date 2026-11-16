package com.ofdun.jobfinder.features.encrypt;

import io.micrometer.observation.annotation.Observed;
import org.springframework.security.crypto.bcrypt.BCrypt;
import org.springframework.stereotype.Service;

@Service
public class BCryptEncryptionService implements EncryptionService {
    @Override
    @Observed(
            name = "jobfinder.encrypt.service.encrypt",
            contextualName = "Хеширование пароля",
            lowCardinalityKeyValues = {"feature", "encrypt", "layer", "service"})
    public String encrypt(String content) {
        return BCrypt.hashpw(content, BCrypt.gensalt());
    }

    @Override
    @Observed(
            name = "jobfinder.encrypt.service.matches",
            contextualName = "Сравнение пароля с хешем",
            lowCardinalityKeyValues = {"feature", "encrypt", "layer", "service"})
    public Boolean matches(String rawContent, String encrypted) {
        return BCrypt.checkpw(rawContent, encrypted);
    }
}
