package com.ofdun.jobfinder.features.applicant.domain.service;

import com.ofdun.jobfinder.features.applicant.domain.model.ApplicantModel;

public interface ApplicantService {
    Long createApplicant(ApplicantModel applicantModel);
    ApplicantModel getApplicantById(Long id);
    ApplicantModel updateApplicant(ApplicantModel applicantModel);
    Boolean deleteApplicant(Long id);
}
