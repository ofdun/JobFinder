package com.ofdun.jobfinder.features.vacancy.data.repository;

import com.ofdun.jobfinder.features.vacancy.data.mapper.VacancyMapper;
import com.ofdun.jobfinder.features.vacancy.domain.model.VacancyModel;
import com.ofdun.jobfinder.features.vacancy.domain.repository.VacancyRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class PostgreSQLVacancyRepository implements VacancyRepository {
    private final VacancyJpaRepository vacancyJpaRepository;

    @Override
    public Long createVacancy(VacancyModel vacancyModel) {
        return vacancyJpaRepository.save(VacancyMapper.toEntity(vacancyModel)).getId();
    }

    @Override
    public VacancyModel getVacancyById(Long id) {
        return vacancyJpaRepository.findById(id).map(VacancyMapper::toModel).orElse(null);
    }

    @Override
    public VacancyModel updateVacancy(VacancyModel vacancyModel) {
        return vacancyJpaRepository.save(VacancyMapper.toEntity(vacancyModel)).getId() != null
                ? vacancyModel
                : null;
    }

    @Override
    public Boolean deleteVacancy(Long id) {
        return vacancyJpaRepository
                .findById(id)
                .map(
                        entity -> {
                            vacancyJpaRepository.delete(entity);
                            return true;
                        })
                .orElse(false);
    }
}
