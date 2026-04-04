package com.ofdun.jobfinder.features.vacancy.data.repository;

import com.ofdun.jobfinder.features.vacancy.domain.model.VacancyModel;
import com.ofdun.jobfinder.features.vacancy.domain.repository.VacancyRepository;
import org.springframework.stereotype.Component;

@Component
public class PostgreSQLVacancyRepository implements VacancyRepository {
    @Override
    public Long createVacancy(VacancyModel vacancyModel) {
        return 0L;
    }

    @Override
    public VacancyModel getVacancyById(Long id) {
        return null;
    }

    @Override
    public VacancyModel updateVacancy(VacancyModel vacancyModel) {
        return vacancyModel;
    }

    @Override
    public Boolean deleteVacancy(Long id) {
        return false;
    }
}
