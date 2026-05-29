package com.ofdun.jobfinder.features.applicant.data.postgres.repository;

import com.ofdun.jobfinder.features.applicant.data.postgres.mapper.ApplicantPostgreSQLMapper;
import com.ofdun.jobfinder.features.applicant.domain.model.ApplicantModel;
import com.ofdun.jobfinder.features.applicant.domain.repository.ApplicantRepository;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@ConditionalOnProperty(name = "app.persistence.type", havingValue = "postgres")
public class PostgreSQLApplicantRepository implements ApplicantRepository {
    private final ApplicantJpaRepository applicantJpaRepository;

    @Override
    public Long createApplicant(ApplicantModel applicantModel) {
        return applicantJpaRepository.save(ApplicantPostgreSQLMapper.toEntity(applicantModel)).getId();
    }

    @Override
    public Optional<ApplicantModel> getApplicantById(Long id) {
        return applicantJpaRepository.findById(id).map(ApplicantPostgreSQLMapper::toModel);
    }

    @Override
    public Optional<ApplicantModel> getApplicantByEmail(String email) {
        return applicantJpaRepository.findByEmail(email).map(ApplicantPostgreSQLMapper::toModel);
    }

    @Override
    public ApplicantModel updateApplicant(ApplicantModel applicantModel) {
        var entity = applicantJpaRepository.save(ApplicantPostgreSQLMapper.toEntity(applicantModel));
        return ApplicantPostgreSQLMapper.toModel(entity);
    }

    @Override
    public Boolean deleteApplicant(Long id) {
        return applicantJpaRepository
                .findById(id)
                .map(
                        entity -> {
                            applicantJpaRepository.delete(entity);
                            return true;
                        })
                .orElse(false);
    }
}
