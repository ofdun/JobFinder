package com.ofdun.jobfinder.features.applicant.data.repository;

import com.ofdun.jobfinder.features.applicant.domain.model.ApplicantModel;
import com.ofdun.jobfinder.features.applicant.domain.repository.ApplicantRepository;
import org.springframework.stereotype.Component;

@Component
public class PostgreSQLApplicantRepository implements ApplicantRepository {
    @Override
    public Long createApplicant(ApplicantModel applicantModel) {
        return 0L;
    }

    @Override
    public ApplicantModel getApplicantById(Long id) {
        return null;
    }

    @Override
    public ApplicantModel getApplicantByEmail(String email) {
        return null;
    }

    @Override
    public ApplicantModel updateApplicant(ApplicantModel applicantModel) {
        return applicantModel;
    }

    @Override
    public Boolean deleteApplicant(Long id) {
        return false;
    }
}
