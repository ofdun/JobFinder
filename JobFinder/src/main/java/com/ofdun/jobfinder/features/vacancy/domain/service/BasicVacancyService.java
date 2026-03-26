package com.ofdun.jobfinder.features.vacancy.domain.service;

import com.ofdun.jobfinder.features.vacancy.domain.model.VacancyModel;
import com.ofdun.jobfinder.features.vacancy.domain.repository.VacancyRepository;
import lombok.NonNull;
import org.springframework.stereotype.Service;

@Service
public class BasicVacancyService implements VacancyService {
    VacancyRepository vacancyRepository;

    @Override
    @NonNull
    public Long createVacancy(VacancyModel vacancyModel) {
        return vacancyRepository.createVacancy(vacancyModel);
    }

    @Override
    @NonNull
    public VacancyModel getVacancyById(Long id) {
        return vacancyRepository.getVacancyById(id);
    }

    @Override
    @NonNull
    public VacancyModel updateVacancy(VacancyModel vacancyModel) {
        return vacancyRepository.updateVacancy(vacancyModel);
    }

    @Override
    @NonNull
    public Boolean deleteVacancy(Long id) {
        return vacancyRepository.deleteVacancy(id);
    }
}
