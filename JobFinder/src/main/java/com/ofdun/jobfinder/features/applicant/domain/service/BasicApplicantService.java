package com.ofdun.jobfinder.features.applicant.domain.service;

import com.ofdun.jobfinder.features.applicant.domain.model.ApplicantModel;
import com.ofdun.jobfinder.features.applicant.domain.repository.ApplicantRepository;
import lombok.NonNull;
import org.springframework.stereotype.Service;

@Service
public class BasicApplicantService implements ApplicantService {
    private final ApplicantRepository applicantRepository;

    public BasicApplicantService(@NonNull ApplicantRepository applicantRepository) {
        this.applicantRepository = applicantRepository;
    }

    @Override
    public Long createApplicant(@NonNull ApplicantModel applicantModel) {
        if (applicantRepository.getApplicantByEmail(applicantModel.getEmail()) != null) {
            throw new IllegalArgumentException("Applicant with email " + applicantModel.getEmail() + " already exists");
        }
        return applicantRepository.createApplicant(applicantModel);
    }

    @Override
    public ApplicantModel getApplicantById(@NonNull Long id) {
        return applicantRepository.getApplicantById(id);
    }

    @Override
    public ApplicantModel updateApplicant(@NonNull ApplicantModel applicantModel) {
        if (applicantRepository.getApplicantById(applicantModel.getId()) == null) {
            throw new IllegalArgumentException("Applicant with id " + applicantModel.getId() + " does not exist");
        }
        return applicantRepository.updateApplicant(applicantModel);
    }

    @Override
    public Boolean deleteApplicant(@NonNull Long id) {
        if (!applicantRepository.deleteApplicant(id)) {
            throw new IllegalArgumentException("Applicant with id " + id + " does not exist");
        }
        return true;
    }
}
