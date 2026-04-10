package com.ofdun.jobfinder.features.auth.data.repository;

import com.ofdun.jobfinder.features.applicant.domain.repository.ApplicantRepository;
import com.ofdun.jobfinder.features.auth.data.mapper.ApplicantAccountMapper;
import com.ofdun.jobfinder.features.auth.domain.model.ApplicantAccountModel;
import com.ofdun.jobfinder.features.auth.domain.repository.ApplicantAccountRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
@RequiredArgsConstructor
public class PostgreSQLApplicantAccountRepository implements ApplicantAccountRepository {
    private final ApplicantRepository applicantRepository;

    @Override
    public Optional<ApplicantAccountModel> findByEmail(String email) {
        return applicantRepository.getApplicantByEmail(email)
                .map(ApplicantAccountMapper::toModel);
    }
}
