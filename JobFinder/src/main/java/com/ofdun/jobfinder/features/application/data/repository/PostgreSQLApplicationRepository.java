package com.ofdun.jobfinder.features.application.data.repository;

import com.ofdun.jobfinder.features.application.data.mapper.ApplicationMapper;
import com.ofdun.jobfinder.features.application.domain.model.ApplicationModel;
import com.ofdun.jobfinder.features.application.domain.repository.ApplicationRepository;
import org.springframework.stereotype.Component;

@Component
public class PostgreSQLApplicationRepository implements ApplicationRepository {
    private final ApplicationJpaRepository applicationJpaRepository;

    public PostgreSQLApplicationRepository(ApplicationJpaRepository applicationJpaRepository) {
        this.applicationJpaRepository = applicationJpaRepository;
    }

    @Override
    public Long saveApplication(ApplicationModel application) {
        return applicationJpaRepository
                .save(ApplicationMapper.toEntity(application))
                .getId();
    }

    @Override
    public ApplicationModel getApplication(Long id) {
        var entity = applicationJpaRepository
                .findById(id)
                .orElse(null);
        return entity != null ? ApplicationMapper.toModel(entity) : null;
    }

    @Override
    public ApplicationModel updateApplication(ApplicationModel application) {
        var entity = ApplicationMapper.toEntity(application);
        return ApplicationMapper.toModel(applicationJpaRepository.save(entity));
    }

    @Override
    public Boolean deleteApplication(Long id) {
        return applicationJpaRepository.findById(id)
                .map(entity -> {
                    applicationJpaRepository.delete(entity);
                    return true;
                })
                .orElse(false);
    }
}
