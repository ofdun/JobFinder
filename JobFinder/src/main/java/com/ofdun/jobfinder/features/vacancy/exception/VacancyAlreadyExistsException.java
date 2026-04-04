package com.ofdun.jobfinder.features.vacancy.exception;

public class VacancyAlreadyExistsException extends RuntimeException {
    public VacancyAlreadyExistsException(Long id) {
        super("Vacancy with ID " + id + " already exists");
    }
}
