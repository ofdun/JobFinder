package com.ofdun.jobfinder.features.application.data.postgres.repository;

import com.ofdun.jobfinder.features.application.data.postgres.mapper.ApplicationPostgreSQLMapper;
import com.ofdun.jobfinder.features.application.domain.model.ApplicationModel;
import com.ofdun.jobfinder.features.application.domain.repository.ApplicationRepository;
import java.util.List;
import java.util.Optional;

import lombok.RequiredArgsConstructor;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@ConditionalOnProperty(name = "app.persistence.type", havingValue = "postgres")
public class PostgreSQLApplicationRepository implements ApplicationRepository {
    private final ApplicationJpaRepository applicationJpaRepository;

    @Override
    public Long createApplication(ApplicationModel application) {
        return applicationJpaRepository.save(ApplicationPostgreSQLMapper.toEntity(application)).getId();
    }

    @Override
    public Optional<ApplicationModel> getApplicationById(Long id) {
        return applicationJpaRepository.findById(id).map(ApplicationPostgreSQLMapper::toModel);
    }

    @Override
    public List<ApplicationModel> getApplicationsByVacancyId(Long vacancyId) {
        return applicationJpaRepository.findByVacancyIdOrderByDateDesc(vacancyId).stream()
                .map(ApplicationPostgreSQLMapper::toModel)
                .toList();
    }

    @Override
    public ApplicationModel updateApplication(ApplicationModel application) {
        var entity = ApplicationPostgreSQLMapper.toEntity(application);
        return ApplicationPostgreSQLMapper.toModel(applicationJpaRepository.save(entity));
    }

    @Override
    public Boolean deleteApplication(Long id) {
        return applicationJpaRepository
                .findById(id)
                .map(
                        entity -> {
                            applicationJpaRepository.delete(entity);
                            return true;
                        })
                .orElse(false);
    }
}
