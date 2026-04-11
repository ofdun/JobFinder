package com.ofdun.jobfinder.features.applicant.domain.repository;

import com.ofdun.jobfinder.features.applicant.domain.model.ApplicantModel;
import java.util.Optional;

public interface ApplicantRepository {
    Long createApplicant(ApplicantModel applicantModel);

    Optional<ApplicantModel> getApplicantById(Long id);

    Optional<ApplicantModel> getApplicantByEmail(String email);

    ApplicantModel updateApplicant(ApplicantModel applicantModel);

    Boolean deleteApplicant(Long id);
}
