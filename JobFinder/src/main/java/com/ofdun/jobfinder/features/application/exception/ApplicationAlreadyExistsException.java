package com.ofdun.jobfinder.features.application.exception;

public class ApplicationAlreadyExistsException extends RuntimeException {
    public ApplicationAlreadyExistsException(Long id) {
        super("Application with id " + id + " already exists.");
    }
}
