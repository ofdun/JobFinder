package com.ofdun.jobfinder.features.application.domain.repository;

import com.ofdun.jobfinder.features.application.domain.model.ApplicationModel;

import java.util.Optional;

public interface ApplicationRepository {
    Long createApplication(ApplicationModel application);
    Optional<ApplicationModel> getApplicationById(Long id);
    ApplicationModel updateApplication(ApplicationModel application);
    Boolean deleteApplication(Long id);
}
