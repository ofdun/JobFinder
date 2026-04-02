package com.ofdun.jobfinder.features.vacancy.exception;

public class VacancyNotFoundException extends RuntimeException {
    public VacancyNotFoundException(Long id) {
        super("Vacancy with ID " + id + " not found");
    }
}
