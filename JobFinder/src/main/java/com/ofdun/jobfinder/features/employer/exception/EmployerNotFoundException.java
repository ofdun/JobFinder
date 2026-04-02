package com.ofdun.jobfinder.features.employer.exception;

public class EmployerNotFoundException extends RuntimeException {
    public EmployerNotFoundException(Long id) {
        super("Employer with ID " + id + " not found");
    }
}
