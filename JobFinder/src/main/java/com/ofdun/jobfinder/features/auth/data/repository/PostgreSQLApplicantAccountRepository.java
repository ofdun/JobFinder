package com.ofdun.jobfinder.features.auth.data.repository;

import com.ofdun.jobfinder.features.applicant.domain.repository.ApplicantRepository;
import com.ofdun.jobfinder.features.auth.data.mapper.ApplicantAccountMapper;
import com.ofdun.jobfinder.features.auth.domain.model.ApplicantAccountModel;
import com.ofdun.jobfinder.features.auth.domain.repository.ApplicantAccountRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class PostgreSQLApplicantAccountRepository implements ApplicantAccountRepository {
    private final ApplicantRepository applicantRepository;

    @Override
    public ApplicantAccountModel findByEmail(String email) {
        var applicant = applicantRepository.getApplicantByEmail(email);
        return applicant == null ? null : ApplicantAccountMapper.toModel(applicant);
    }
}
