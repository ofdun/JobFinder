package com.ofdun.jobfinder.features.vacancy.domain.repository;

import com.ofdun.jobfinder.features.vacancy.domain.model.VacancyModel;

public interface VacancyRepository {
    Long createVacancy(VacancyModel vacancyModel);
    VacancyModel getVacancyById(Long id);
    VacancyModel updateVacancy(VacancyModel vacancyModel);
    Boolean deleteVacancy(Long id);
}
