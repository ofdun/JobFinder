package com.ofdun.jobfinder.features.application.domain.service;

import com.ofdun.jobfinder.features.application.domain.model.ApplicationModel;

public interface ApplicationService {
    Long saveApplication(ApplicationModel application);
    ApplicationModel getApplication(Long id);
    ApplicationModel updateApplication(ApplicationModel application);
    Boolean deleteApplication(Long id);
}
