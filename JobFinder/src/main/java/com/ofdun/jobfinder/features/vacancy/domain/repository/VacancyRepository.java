package com.ofdun.jobfinder.features.vacancy.domain.repository;

import com.ofdun.jobfinder.common.domain.model.OffsetPagination;
import com.ofdun.jobfinder.common.domain.model.PageResult;
import com.ofdun.jobfinder.features.vacancy.domain.model.VacancyModel;
import com.ofdun.jobfinder.features.vacancy.domain.model.VacancySearchFilter;
import java.util.Optional;

public interface VacancyRepository {
    Long createVacancy(VacancyModel vacancyModel);

    Optional<VacancyModel> getVacancyById(Long id);

    VacancyModel updateVacancy(VacancyModel vacancyModel);

    Boolean deleteVacancy(Long id);

    PageResult<VacancyModel> searchVacancies(
            VacancySearchFilter filter, OffsetPagination pagination);
}
