package com.ofdun.jobfinder.features.vacancy.domain.service;

import com.ofdun.jobfinder.features.vacancy.domain.model.VacancyModel;

public interface VacancyService {
    Long createVacancy(VacancyModel vacancyModel);
    VacancyModel getVacancyById(Long id);
    VacancyModel updateVacancy(VacancyModel vacancyModel);
    Boolean deleteVacancy(Long id);
}
