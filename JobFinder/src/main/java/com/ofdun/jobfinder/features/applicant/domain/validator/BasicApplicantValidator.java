package com.ofdun.jobfinder.features.applicant.domain.validator; // java

import com.ofdun.jobfinder.features.applicant.domain.model.ApplicantModel;
import com.ofdun.jobfinder.features.applicant.domain.repository.ApplicantRepository;
import com.ofdun.jobfinder.features.applicant.exception.ApplicantAlreadyExistsException;
import com.ofdun.jobfinder.features.applicant.exception.ApplicantNotFoundException;
import io.micrometer.observation.annotation.Observed;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class BasicApplicantValidator implements ApplicantValidator {
    private final ApplicantRepository applicantRepository;

    @Observed(
            name = "jobfinder.applicant.validation.validateApplicantForCreate",
            contextualName = "Проверка соискателя перед созданием",
            lowCardinalityKeyValues = {"feature", "applicant", "layer", "validation"})
    public void validateApplicantForCreate(ApplicantModel model) {
        validateAlreadyExists(model.getEmail());
    }

    @Observed(
            name = "jobfinder.applicant.validation.validateApplicantForUpdate",
            contextualName = "Проверка соискателя перед обновлением",
            lowCardinalityKeyValues = {"feature", "applicant", "layer", "validation"})
    public void validateApplicantForUpdate(ApplicantModel model) {
        validateId(model.getId());
        validateDoesNotExist(model.getId());
    }

    @Observed(
            name = "jobfinder.applicant.validation.validateApplicantForDelete",
            contextualName = "Проверка соискателя перед удалением",
            lowCardinalityKeyValues = {"feature", "applicant", "layer", "validation"})
    public void validateApplicantForDelete(Long id) {
        validateId(id);
        validateDoesNotExist(id);
    }

    private void validateAlreadyExists(String email) {
        if (applicantRepository.getApplicantByEmail(email).isPresent()) {
            throw new ApplicantAlreadyExistsException(email);
        }
    }

    private void validateDoesNotExist(Long id) {
        if (applicantRepository.getApplicantById(id).isEmpty()) {
            throw new ApplicantNotFoundException(id);
        }
    }

    private void validateId(Long id) {
        if (id == null || id <= 0) {
            throw new IllegalArgumentException("Applicant id must be a positive number");
        }
    }
}
