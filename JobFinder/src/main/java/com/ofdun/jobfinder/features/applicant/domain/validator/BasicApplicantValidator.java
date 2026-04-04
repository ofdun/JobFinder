package com.ofdun.jobfinder.features.applicant.domain.validator;// java

import com.ofdun.jobfinder.features.applicant.domain.model.ApplicantModel;
import com.ofdun.jobfinder.features.applicant.domain.repository.ApplicantRepository;
import com.ofdun.jobfinder.features.applicant.exception.ApplicantAlreadyExistsException;
import com.ofdun.jobfinder.features.applicant.exception.ApplicantNotFoundException;
import org.springframework.stereotype.Component;
import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class BasicApplicantValidator implements ApplicantValidator {
    private final ApplicantRepository applicantRepository;

    public void validateApplicantForCreate(ApplicantModel model) {
        validateAlreadyExists(model.getEmail());
    }

    public void validateApplicantForUpdate(ApplicantModel model) {
        validateId(model.getId());
        validateDoesNotExist(model.getId());
    }

    public void validateApplicantForDelete(Long id) {
        validateId(id);
        validateDoesNotExist(id);
    }

    private void validateAlreadyExists(String email) {
        if (applicantRepository.getApplicantByEmail(email) != null) {
            throw new ApplicantAlreadyExistsException(email);
        }
    }

    private void validateDoesNotExist(Long id) {
        if (applicantRepository.getApplicantById(id) == null) {
            throw new ApplicantNotFoundException(id);
        }
    }

    private void validateId(Long id) {
        if (id == null || id <= 0) {
            throw new IllegalArgumentException("Applicant id must be a positive number");
        }
    }
}
