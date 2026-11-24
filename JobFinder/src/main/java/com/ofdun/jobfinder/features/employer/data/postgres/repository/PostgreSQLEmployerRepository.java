package com.ofdun.jobfinder.features.employer.data.postgres.repository;

import com.ofdun.jobfinder.features.employer.data.postgres.mapper.EmployerMapper;
import com.ofdun.jobfinder.features.employer.domain.model.EmployerModel;
import com.ofdun.jobfinder.features.employer.domain.repository.EmployerRepository;
import io.micrometer.observation.annotation.Observed;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@ConditionalOnProperty(name = "app.persistence.type", havingValue = "postgres")
public class PostgreSQLEmployerRepository implements EmployerRepository {
    private final EmployerJpaRepository jpaRepository;

    @Override
    @Observed(
            name = "jobfinder.employer.repository.createEmployer",
            contextualName = "PostgreSQL: создание работодателя",
            lowCardinalityKeyValues = {
                "feature",
                "employer",
                "layer",
                "repository",
                "storage",
                "PostgreSQL"
            })
    public Long createEmployer(EmployerModel employerModel) {
        return jpaRepository.save(EmployerMapper.toEntity(employerModel)).getId();
    }

    @Override
    @Observed(
            name = "jobfinder.employer.repository.getEmployerById",
            contextualName = "PostgreSQL: получение работодателя по ID",
            lowCardinalityKeyValues = {
                "feature",
                "employer",
                "layer",
                "repository",
                "storage",
                "PostgreSQL"
            })
    public Optional<EmployerModel> getEmployerById(Long id) {
        return jpaRepository.findById(id).map(EmployerMapper::toModel);
    }

    @Override
    @Observed(
            name = "jobfinder.employer.repository.getEmployerByEmail",
            contextualName = "PostgreSQL: получение работодателя по почте",
            lowCardinalityKeyValues = {
                "feature",
                "employer",
                "layer",
                "repository",
                "storage",
                "PostgreSQL"
            })
    public Optional<EmployerModel> getEmployerByEmail(String email) {
        return jpaRepository.findByEmail(email).map(EmployerMapper::toModel);
    }

    @Override
    @Observed(
            name = "jobfinder.employer.repository.updateEmployer",
            contextualName = "PostgreSQL: обновление работодателя",
            lowCardinalityKeyValues = {
                "feature",
                "employer",
                "layer",
                "repository",
                "storage",
                "PostgreSQL"
            })
    public EmployerModel updateEmployer(EmployerModel employerModel) {
        var entity = jpaRepository.save(EmployerMapper.toEntity(employerModel));
        return EmployerMapper.toModel(entity);
    }

    @Override
    @Observed(
            name = "jobfinder.employer.repository.deleteEmployer",
            contextualName = "PostgreSQL: удаление работодателя",
            lowCardinalityKeyValues = {
                "feature",
                "employer",
                "layer",
                "repository",
                "storage",
                "PostgreSQL"
            })
    public Boolean deleteEmployer(Long id) {
        return jpaRepository
                .findById(id)
                .map(
                        entity -> {
                            jpaRepository.delete(entity);
                            return true;
                        })
                .orElse(false);
    }
}
