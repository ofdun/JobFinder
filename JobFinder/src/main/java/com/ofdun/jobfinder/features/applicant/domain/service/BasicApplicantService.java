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
    @org.springframework.beans.factory.annotation.Autowired
    private com.ofdun.jobfinder.features.auth.mfa.AuthenticationProtection protection;

    private final ApplicantRepository applicantRepository;
    private final ApplicantValidator applicantValidator;

    @Override
    public Long createApplicant(@NonNull @Valid ApplicantModel applicantModel) {
        applicantValidator.validateApplicantForCreate(applicantModel);
        return applicantRepository.createApplicant(applicantModel);
    }

    @Override
    public ApplicantModel getApplicantById(@NonNull Long id) {
        return applicantRepository
                .getApplicantById(id)
                .orElseThrow(() -> new ApplicantNotFoundException(id));
    }

    @Override
    public ApplicantModel updateApplicant(@NonNull @Valid ApplicantModel applicantModel) {
        if (protection != null)
            protection.checkProfilePasswordChange(
                    com.ofdun.jobfinder.features.auth.enums.AccountType.APPLICANT,
                    applicantModel.getId(),
                    applicantModel.getPasswordHash());
        applicantValidator.validateApplicantForUpdate(applicantModel);
        return applicantRepository.updateApplicant(applicantModel);
    }

    @Override
    public Boolean deleteApplicant(@NonNull Long id) {
        applicantValidator.validateApplicantForDelete(id);
        var deleted = applicantRepository.deleteApplicant(id);
        if (Boolean.TRUE.equals(deleted) && protection != null)
            protection.remove(com.ofdun.jobfinder.features.auth.enums.AccountType.APPLICANT, id);
        return deleted;
    }
}
