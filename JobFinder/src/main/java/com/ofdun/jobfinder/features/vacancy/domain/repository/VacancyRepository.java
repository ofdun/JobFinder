package com.ofdun.jobfinder.features.vacancy.domain.repository;

import com.ofdun.jobfinder.features.vacancy.domain.model.VacancyModel;

import java.util.Optional;

public interface VacancyRepository {
    Long createVacancy(VacancyModel vacancyModel);
    Optional<VacancyModel> getVacancyById(Long id);
    VacancyModel updateVacancy(VacancyModel vacancyModel);
    Boolean deleteVacancy(Long id);
}
