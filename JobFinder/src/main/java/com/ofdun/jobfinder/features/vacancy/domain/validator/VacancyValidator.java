package com.ofdun.jobfinder.features.vacancy.domain.validator;

import com.ofdun.jobfinder.features.vacancy.domain.model.VacancyModel;

public interface VacancyValidator {
    void validateVacancyForCreate(VacancyModel vacancy);
    void validateVacancyForUpdate(VacancyModel vacancy);
    void validateVacancyForDelete(Long vacancyId);
}
