package com.ofdun.jobfinder.features.application.data.repository;

import com.ofdun.jobfinder.features.application.domain.model.ApplicationModel;
import com.ofdun.jobfinder.features.application.domain.repository.ApplicationRepository;
import org.springframework.stereotype.Component;

@Component
public class PostgreSQLApplicationRepository implements ApplicationRepository {
    @Override
    public Long saveApplication(ApplicationModel application) {
        return 0L;
    }

    @Override
    public ApplicationModel getApplication(Long id) {
        return null;
    }

    @Override
    public ApplicationModel updateApplication(ApplicationModel application) {
        return application;
    }

    @Override
    public Boolean deleteApplication(Long id) {
        return false;
    }
}
