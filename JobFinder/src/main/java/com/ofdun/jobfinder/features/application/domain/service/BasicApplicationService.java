package com.ofdun.jobfinder.features.application.domain.service;

import com.ofdun.jobfinder.features.application.domain.model.ApplicationModel;
import com.ofdun.jobfinder.features.application.domain.repository.ApplicationRepository;
import lombok.NonNull;
import org.springframework.stereotype.Service;

@Service
public class BasicApplicationService implements  ApplicationService {
    private final ApplicationRepository applicationRepository;

    public BasicApplicationService(ApplicationRepository applicationRepository) {
        this.applicationRepository = applicationRepository;
    }

    @Override
    public Long saveApplication(@NonNull ApplicationModel application) {
        return applicationRepository.saveApplication(application);
    }

    @Override
    public ApplicationModel getApplication(@NonNull Long id) {
        return applicationRepository.getApplication(id);
    }

    @Override
    public ApplicationModel updateApplication(@NonNull ApplicationModel application) {
        if (applicationRepository.getApplication(application.getId()) == null) {
            throw new IllegalArgumentException("Application with id " + application.getId() + " does not exist.");
        }
        return applicationRepository.updateApplication(application);
    }

    @Override
    public Boolean deleteApplication(@NonNull Long id) {
        if (!applicationRepository.deleteApplication(id)) {
            throw new IllegalArgumentException("Application with id " + id + " does not exist.");
        }
        return true;
    }
}
