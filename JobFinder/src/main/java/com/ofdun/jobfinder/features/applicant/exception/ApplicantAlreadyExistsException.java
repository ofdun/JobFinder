package com.ofdun.jobfinder.features.applicant.exception;

public class ApplicantAlreadyExistsException extends RuntimeException {
    public ApplicantAlreadyExistsException(String email) {
        super("Applicant with string " + email + " already exists");
    }
}
