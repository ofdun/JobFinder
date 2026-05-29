package com.ofdun.jobfinder.features.employer.data.postgres.repository;

import com.ofdun.jobfinder.features.employer.data.postgres.mapper.EmployerMapper;
import com.ofdun.jobfinder.features.employer.domain.model.EmployerModel;
import com.ofdun.jobfinder.features.employer.domain.repository.EmployerRepository;
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
    public Long createEmployer(EmployerModel employerModel) {
        return jpaRepository.save(EmployerMapper.toEntity(employerModel)).getId();
    }

    @Override
    public Optional<EmployerModel> getEmployerById(Long id) {
        return jpaRepository.findById(id).map(EmployerMapper::toModel);
    }

    @Override
    public Optional<EmployerModel> getEmployerByEmail(String email) {
        return jpaRepository.findByEmail(email).map(EmployerMapper::toModel);
    }

    @Override
    public EmployerModel updateEmployer(EmployerModel employerModel) {
        var entity = jpaRepository.save(EmployerMapper.toEntity(employerModel));
        return EmployerMapper.toModel(entity);
    }

    @Override
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
