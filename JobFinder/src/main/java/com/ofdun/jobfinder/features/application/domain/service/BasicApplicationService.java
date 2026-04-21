package com.ofdun.jobfinder.features.application.domain.service;

import com.ofdun.jobfinder.features.application.domain.model.ApplicationModel;
import com.ofdun.jobfinder.features.application.domain.repository.ApplicationRepository;
import com.ofdun.jobfinder.features.application.domain.validator.ApplicationValidator;
import com.ofdun.jobfinder.features.application.exception.ApplicationNotFoundException;
import java.util.List;
import jakarta.validation.Valid;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class BasicApplicationService implements ApplicationService {
    private final ApplicationRepository applicationRepository;
    private final ApplicationValidator applicationValidator;

    @Override
    public Long saveApplication(@NonNull @Valid ApplicationModel application) {
        applicationValidator.validateApplicationForCreate(application);
        return applicationRepository.createApplication(application);
    }

    @Override
    public ApplicationModel getApplication(@NonNull Long id) {
        return applicationRepository
                .getApplicationById(id)
                .orElseThrow(() -> new ApplicationNotFoundException(id));
    }

    @Override
    public List<ApplicationModel> getApplicationsByVacancyId(@NonNull Long vacancyId) {
        if (vacancyId <= 0) {
            throw new IllegalArgumentException("Vacancy id must be a positive number");
        }

        return applicationRepository.getApplicationsByVacancyId(vacancyId);
    }

    @Override
    public ApplicationModel updateApplication(@NonNull @Valid ApplicationModel application) {
        applicationValidator.validateApplicationForUpdate(application);
        return applicationRepository.updateApplication(application);
    }

    @Override
    public Boolean deleteApplication(@NonNull Long id) {
        applicationValidator.validateApplicationForDelete(id);
        return applicationRepository.deleteApplication(id);
    }
}
