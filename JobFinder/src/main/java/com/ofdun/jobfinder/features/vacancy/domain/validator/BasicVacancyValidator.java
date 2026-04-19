package com.ofdun.jobfinder.features.vacancy.domain.validator;

import com.ofdun.jobfinder.features.vacancy.domain.model.VacancyModel;
import com.ofdun.jobfinder.features.vacancy.domain.repository.VacancyRepository;
import com.ofdun.jobfinder.features.vacancy.exception.VacancyAlreadyExistsException;
import com.ofdun.jobfinder.features.vacancy.exception.VacancyNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class BasicVacancyValidator implements VacancyValidator {
    private final VacancyRepository vacancyRepository;

    @Override
    public void validateVacancyForCreate(VacancyModel vacancy) {}

    @Override
    public void validateVacancyForUpdate(VacancyModel vacancy) {
        validateId(vacancy.getId());
        validateVacancyDoesNotExist(vacancy.getId());
    }

    @Override
    public void validateVacancyForDelete(Long vacancyId) {
        validateId(vacancyId);
        validateVacancyExists(vacancyId);
    }

    private void validateVacancyExists(Long id) {
        if (vacancyRepository.getVacancyById(id).isEmpty()) {
            throw new VacancyNotFoundException(id);
        }
    }

    private void validateVacancyDoesNotExist(Long id) {
        if (vacancyRepository.getVacancyById(id).isPresent()) {
            throw new VacancyAlreadyExistsException(id);
        }
    }

    private void validateId(Long id) {
        if (id == null || id <= 0) {
            throw new IllegalArgumentException("Vacancy ID must be a positive number.");
        }
    }
}
