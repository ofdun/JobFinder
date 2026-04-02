package com.ofdun.jobfinder.features.applicant.exception;

public class ApplicantNotFoundException extends RuntimeException {
    public ApplicantNotFoundException(Long id) {
        super("Applicant with id " + id + " not found");
    }
}
