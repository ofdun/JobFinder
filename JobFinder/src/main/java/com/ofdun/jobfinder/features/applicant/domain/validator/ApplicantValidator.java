package com.ofdun.jobfinder.features.applicant.domain.validator;

import com.ofdun.jobfinder.features.applicant.domain.model.ApplicantModel;

public interface ApplicantValidator {
    void validateApplicantForCreate(ApplicantModel model);
    void validateApplicantForUpdate(ApplicantModel model);
    void validateApplicantForDelete(Long id);
}
