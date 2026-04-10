package com.ofdun.jobfinder.features.application.exception;

public class ApplicationNotFoundException extends RuntimeException {
    public ApplicationNotFoundException(Long id) {
        super("Application with ID " + id + " not found");
    }
}
