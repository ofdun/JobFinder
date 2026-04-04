package com.ofdun.jobfinder.features.vacancy.domain.service;

import com.ofdun.jobfinder.features.vacancy.domain.model.VacancyModel;
import com.ofdun.jobfinder.features.vacancy.domain.repository.VacancyRepository;
import com.ofdun.jobfinder.features.vacancy.domain.validator.VacancyValidator;
import jakarta.validation.Valid;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class BasicVacancyService implements VacancyService {
    private final VacancyRepository vacancyRepository;
    private final VacancyValidator vacancyValidator;

    @Override
    public Long createVacancy(@NonNull @Valid VacancyModel vacancyModel) {
        vacancyValidator.validateVacancyForCreate(vacancyModel);
        return vacancyRepository.createVacancy(vacancyModel);
    }

    @Override
    public VacancyModel getVacancyById(@NonNull Long id) {
        return vacancyRepository.getVacancyById(id);
    }

    @Override
    public VacancyModel updateVacancy(@NonNull @Valid VacancyModel vacancyModel) {
        vacancyValidator.validateVacancyForUpdate(vacancyModel);
        return vacancyRepository.updateVacancy(vacancyModel);
    }

    @Override
    public Boolean deleteVacancy(@NonNull Long id) {
        vacancyValidator.validateVacancyForDelete(id);
        return vacancyRepository.deleteVacancy(id);
    }
}

