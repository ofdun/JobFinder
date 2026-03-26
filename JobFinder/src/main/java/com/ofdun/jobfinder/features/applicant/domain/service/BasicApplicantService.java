package com.ofdun.jobfinder.features.applicant.domain.service;

import com.ofdun.jobfinder.features.applicant.domain.model.ApplicantModel;
import com.ofdun.jobfinder.features.applicant.domain.repository.ApplicantRepository;
import lombok.NonNull;
import org.springframework.stereotype.Service;

@Service
public class BasicApplicantService implements ApplicantService {
    ApplicantRepository applicantRepository;

    @Override
    @NonNull
    public Long createApplicant(ApplicantModel applicantModel) {
        return applicantRepository.createApplicant(applicantModel);
    }

    @Override
    @NonNull
    public ApplicantModel getApplicantById(Long id) {
        return applicantRepository.getApplicantById(id);
    }

    @Override
    @NonNull
    public ApplicantModel updateApplicant(ApplicantModel applicantModel) {
        return applicantRepository.updateApplicant(applicantModel);
    }

    @Override
    @NonNull
    public Boolean deleteApplicant(Long id) {
        return applicantRepository.deleteApplicant(id);
    }
}
