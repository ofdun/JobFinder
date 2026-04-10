package com.ofdun.jobfinder.features.resume.exception;

public class FailedToCreateResumeException extends RuntimeException {
    public FailedToCreateResumeException(Long applicantId) {
        super("Failed to create resume for applicant with ID " + applicantId);
    }
}
