package com.ofdun.jobfinder.features.auth.data.repository;

import com.ofdun.jobfinder.features.applicant.domain.repository.ApplicantRepository;
import com.ofdun.jobfinder.features.auth.data.mapper.ApplicantAccountMapper;
import com.ofdun.jobfinder.features.auth.domain.model.ApplicantAccountModel;
import com.ofdun.jobfinder.features.auth.domain.repository.ApplicantAccountRepository;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@ConditionalOnProperty(name = "app.persistence.type", havingValue = "mongo")
public class MongoApplicantAccountRepository implements ApplicantAccountRepository {
    private final ApplicantRepository applicantRepository;

    @Override
    public Optional<ApplicantAccountModel> findByEmail(String email) {
        return applicantRepository.getApplicantByEmail(email).map(ApplicantAccountMapper::toModel);
    }
}

