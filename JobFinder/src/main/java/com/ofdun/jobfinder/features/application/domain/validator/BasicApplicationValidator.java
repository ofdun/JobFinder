package com.ofdun.jobfinder.features.application.domain.validator;

import com.ofdun.jobfinder.features.application.domain.model.ApplicationModel;
import com.ofdun.jobfinder.features.application.domain.repository.ApplicationRepository;
import com.ofdun.jobfinder.features.application.exception.ApplicationAlreadyExistsException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class BasicApplicationValidator implements ApplicationValidator {
    private final ApplicationRepository applicationRepository;

    @Override
    public void validateApplicationForCreate(ApplicationModel model) {
        validateId(model.getResumeId());
        validateId(model.getVacancyId());
    }

    @Override
    public void validateApplicationForUpdate(ApplicationModel model) {
        validateId(model.getId());
        validateId(model.getResumeId());
        validateId(model.getVacancyId());
        validateExists(model.getId());
    }

    @Override
    public void validateApplicationForDelete(Long applicationId) {
        validateId(applicationId);
        validateNotExists(applicationId);
    }

    private void validateNotExists(Long id) {
        if (applicationRepository.getApplicationById(id).isPresent()) {
            throw new ApplicationAlreadyExistsException(id);
        }
    }

    private void validateExists(Long id) {
        if (applicationRepository.getApplicationById(id).isEmpty()) {
            throw new ApplicationAlreadyExistsException(id);
        }
    }

    private void validateId(Long id) {
        if (id == null || id <= 0) {
            throw new IllegalArgumentException("Applicant id must be a positive number");
        }
    }
}
