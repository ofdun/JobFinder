package com.ofdun.jobfinder.features.employer.exception;

public class EmployerAlreadyExistsException extends RuntimeException {
    public EmployerAlreadyExistsException(String email) {
        super("Employer with email " + email + " already exists");
    }
}
