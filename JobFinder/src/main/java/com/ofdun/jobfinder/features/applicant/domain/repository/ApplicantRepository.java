package com.ofdun.jobfinder.features.applicant.domain.repository;

import com.ofdun.jobfinder.features.applicant.domain.model.ApplicantModel;

public interface ApplicantRepository {
    Long createApplicant(ApplicantModel applicantModel);
    ApplicantModel getApplicantById(Long id);
    ApplicantModel getApplicantByEmail(String email);
    ApplicantModel updateApplicant(ApplicantModel applicantModel);
    Boolean deleteApplicant(Long id);
}
