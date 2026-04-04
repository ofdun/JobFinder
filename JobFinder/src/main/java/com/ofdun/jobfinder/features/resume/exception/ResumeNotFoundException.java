package com.ofdun.jobfinder.features.resume.exception;

public class ResumeNotFoundException extends RuntimeException {
    public ResumeNotFoundException(Long id) {
        super("Resume with ID " + id + " not found");
    }
}
