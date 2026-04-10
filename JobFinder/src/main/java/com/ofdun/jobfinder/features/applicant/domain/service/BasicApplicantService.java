package com.ofdun.jobfinder.features.applicant.domain.service;

import com.ofdun.jobfinder.features.applicant.domain.model.ApplicantModel;
import com.ofdun.jobfinder.features.applicant.domain.repository.ApplicantRepository;
import com.ofdun.jobfinder.features.applicant.domain.validator.ApplicantValidator;
import com.ofdun.jobfinder.features.applicant.exception.ApplicantNotFoundException;
import jakarta.validation.Valid;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class BasicApplicantService implements ApplicantService {
    private final ApplicantRepository applicantRepository;
    private final ApplicantValidator applicantValidator;

    @Override
    public Long createApplicant(@NonNull @Valid ApplicantModel applicantModel) {
        applicantValidator.validateApplicantForCreate(applicantModel);
        return applicantRepository.createApplicant(applicantModel);
    }

    @Override
    public ApplicantModel getApplicantById(@NonNull Long id) {
        return applicantRepository.getApplicantById(id)
                .orElseThrow(() -> new ApplicantNotFoundException(id));
    }

    @Override
    public ApplicantModel updateApplicant(@NonNull @Valid ApplicantModel applicantModel) {
        applicantValidator.validateApplicantForUpdate(applicantModel);
        return applicantRepository.updateApplicant(applicantModel);
    }

    @Override
    public Boolean deleteApplicant(@NonNull Long id) {
        applicantValidator.validateApplicantForDelete(id);
        return applicantRepository.deleteApplicant(id);
    }
}
