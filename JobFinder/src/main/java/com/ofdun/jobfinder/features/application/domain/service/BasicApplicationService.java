package com.ofdun.jobfinder.features.application.domain.service;

import com.ofdun.jobfinder.features.application.domain.model.ApplicationModel;
import com.ofdun.jobfinder.features.application.domain.repository.ApplicationRepository;
import org.springframework.stereotype.Service;

@Service
public class BasicApplicationService implements  ApplicationService {
    ApplicationRepository applicationRepository;

    @Override
    public Long saveApplication(ApplicationModel application) {
        return applicationRepository.saveApplication(application);
    }

    @Override
    public ApplicationModel getApplication(Long id) {
        return applicationRepository.getApplication(id);
    }

    @Override
    public ApplicationModel updateApplication(ApplicationModel application) {
        return applicationRepository.updateApplication(application);
    }

    @Override
    public Boolean deleteApplication(Long id) {
        return applicationRepository.deleteApplication(id);
    }
}
