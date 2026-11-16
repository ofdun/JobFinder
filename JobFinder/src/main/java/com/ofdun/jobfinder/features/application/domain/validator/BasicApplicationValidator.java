package com.ofdun.jobfinder.features.application.domain.validator;

import com.ofdun.jobfinder.features.application.domain.model.ApplicationModel;
import com.ofdun.jobfinder.features.application.domain.repository.ApplicationRepository;
import com.ofdun.jobfinder.features.application.exception.ApplicationNotFoundException;
import io.micrometer.observation.annotation.Observed;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class BasicApplicationValidator implements ApplicationValidator {
    private final ApplicationRepository applicationRepository;

    @Override
    @Observed(
            name = "jobfinder.application.validation.validateApplicationForCreate",
            contextualName = "Проверка отклика перед созданием",
            lowCardinalityKeyValues = {"feature", "application", "layer", "validation"})
    public void validateApplicationForCreate(ApplicationModel model) {
        validateId(model.getResumeId());
        validateId(model.getVacancyId());
    }

    @Override
    @Observed(
            name = "jobfinder.application.validation.validateApplicationForUpdate",
            contextualName = "Проверка отклика перед обновлением",
            lowCardinalityKeyValues = {"feature", "application", "layer", "validation"})
    public void validateApplicationForUpdate(ApplicationModel model) {
        validateId(model.getId());
        validateId(model.getResumeId());
        validateId(model.getVacancyId());
        validateExists(model.getId());
    }

    @Override
    @Observed(
            name = "jobfinder.application.validation.validateApplicationForDelete",
            contextualName = "Проверка отклика перед удалением",
            lowCardinalityKeyValues = {"feature", "application", "layer", "validation"})
    public void validateApplicationForDelete(Long applicationId) {
        validateId(applicationId);
        validateExists(applicationId);
    }

    private void validateExists(Long id) {
        if (applicationRepository.getApplicationById(id).isEmpty()) {
            throw new ApplicationNotFoundException(id);
        }
    }

    private void validateId(Long id) {
        if (id == null || id <= 0) {
            throw new IllegalArgumentException("Applicant id must be a positive number");
        }
    }
}
