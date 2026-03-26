package com.ofdun.jobfinder.features.application.domain.repository;

import com.ofdun.jobfinder.features.application.domain.model.ApplicationModel;

public interface ApplicationRepository {
    Long saveApplication(ApplicationModel application);
    ApplicationModel getApplication(Long id);
    ApplicationModel updateApplication(ApplicationModel application);
    Boolean deleteApplication(Long id);
}
