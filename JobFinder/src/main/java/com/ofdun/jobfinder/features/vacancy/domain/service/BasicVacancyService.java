package com.ofdun.jobfinder.features.vacancy.domain.service;

import com.ofdun.jobfinder.features.vacancy.domain.model.VacancyModel;
import com.ofdun.jobfinder.features.vacancy.domain.repository.VacancyRepository;
import lombok.NonNull;
import org.springframework.stereotype.Service;

@Service
public class BasicVacancyService implements VacancyService {
    private final VacancyRepository vacancyRepository;

    public BasicVacancyService(VacancyRepository vacancyRepository) {
        this.vacancyRepository = vacancyRepository;
    }

    @Override
    public Long createVacancy(@NonNull VacancyModel vacancyModel) {
        return vacancyRepository.createVacancy(vacancyModel);
    }

    @Override
    public VacancyModel getVacancyById(@NonNull Long id) {
        return vacancyRepository.getVacancyById(id);
    }

    @Override
    public VacancyModel updateVacancy(@NonNull VacancyModel vacancyModel) {
        if (vacancyRepository.getVacancyById(vacancyModel.getId()) == null) {
            throw new IllegalArgumentException("Vacancy with id " + vacancyModel.getId() + " does not exist.");
        }
        return vacancyRepository.updateVacancy(vacancyModel);
    }

    @Override
    public Boolean deleteVacancy(@NonNull Long id) {
        if (!vacancyRepository.deleteVacancy(id)) {
            throw new IllegalArgumentException("Vacancy with id " + id + " does not exist.");
        }
        return true;
    }
}

