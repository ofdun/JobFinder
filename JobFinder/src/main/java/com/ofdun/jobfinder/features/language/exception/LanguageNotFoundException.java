package com.ofdun.jobfinder.features.language.exception;

public class LanguageNotFoundException extends RuntimeException {
    public LanguageNotFoundException(Long id) {
        super("Language with ID " + id + " not found");
    }
}
