package com.ofdun.jobfinder.features.application.data.repository;

import com.ofdun.jobfinder.features.application.data.mapper.ApplicationMapper;
import com.ofdun.jobfinder.features.application.domain.model.ApplicationModel;
import com.ofdun.jobfinder.features.application.domain.repository.ApplicationRepository;
import java.util.Optional;
import org.springframework.stereotype.Component;

@Component
public class PostgreSQLApplicationRepository implements ApplicationRepository {
    private final ApplicationJpaRepository applicationJpaRepository;

    public PostgreSQLApplicationRepository(ApplicationJpaRepository applicationJpaRepository) {
        this.applicationJpaRepository = applicationJpaRepository;
    }

    @Override
    public Long createApplication(ApplicationModel application) {
        return applicationJpaRepository.save(ApplicationMapper.toEntity(application)).getId();
    }

    @Override
    public Optional<ApplicationModel> getApplicationById(Long id) {
        return applicationJpaRepository.findById(id).map(ApplicationMapper::toModel);
    }

    @Override
    public ApplicationModel updateApplication(ApplicationModel application) {
        var entity = ApplicationMapper.toEntity(application);
        return ApplicationMapper.toModel(applicationJpaRepository.save(entity));
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
