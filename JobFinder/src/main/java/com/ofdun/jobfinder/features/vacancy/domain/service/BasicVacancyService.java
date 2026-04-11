package com.ofdun.jobfinder.features.vacancy.domain.service;

import com.ofdun.jobfinder.common.domain.model.OffsetPagination;
import com.ofdun.jobfinder.common.domain.model.PageResult;
import com.ofdun.jobfinder.features.vacancy.domain.model.VacancyModel;
import com.ofdun.jobfinder.features.vacancy.domain.model.VacancySearchFilter;
import com.ofdun.jobfinder.features.vacancy.domain.repository.VacancyRepository;
import com.ofdun.jobfinder.features.vacancy.domain.validator.VacancyValidator;
import com.ofdun.jobfinder.features.vacancy.exception.VacancyNotFoundException;
import jakarta.validation.Valid;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class BasicVacancyService implements VacancyService {
    private static final int MAX_LIMIT = 100;

    private final VacancyRepository vacancyRepository;
    private final VacancyValidator vacancyValidator;

    @Override
    public Long createVacancy(@NonNull @Valid VacancyModel vacancyModel) {
        vacancyValidator.validateVacancyForCreate(vacancyModel);
        return vacancyRepository.createVacancy(vacancyModel);
    }

    @Override
    public VacancyModel getVacancyById(@NonNull Long id) {
        return vacancyRepository
                .getVacancyById(id)
                .orElseThrow(() -> new VacancyNotFoundException(id));
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

    @Override
    public PageResult<VacancyModel> searchVacancies(
            VacancySearchFilter filter, OffsetPagination pagination) {
        OffsetPagination p = pagination == null ? OffsetPagination.builder().build() : pagination;

        int safeLimit = Math.min(MAX_LIMIT, Math.max(1, p.getLimit()));
        int safeOffset = Math.max(0, p.getOffset());

        if (filter != null && filter.getSalaryMin() != null && filter.getSalaryMax() != null) {
            if (filter.getSalaryMin().compareTo(filter.getSalaryMax()) > 0) {
                throw new IllegalArgumentException("salaryMin must be <= salaryMax");
            }
        }

        return vacancyRepository.searchVacancies(
                filter,
                OffsetPagination.builder()
                        .limit(safeLimit)
                        .offset(safeOffset)
                        .sortBy(p.getSortBy())
                        .sortDesc(p.isSortDesc())
                        .build());
    }
}
