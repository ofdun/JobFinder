package com.ofdun.jobfinder.features.applicant.data.repository;

import com.ofdun.jobfinder.features.applicant.data.mapper.ApplicantMapper;
import com.ofdun.jobfinder.features.applicant.domain.model.ApplicantModel;
import com.ofdun.jobfinder.features.applicant.domain.repository.ApplicantRepository;
import org.springframework.stereotype.Component;

@Component
public class PostgreSQLApplicantRepository implements ApplicantRepository {
    private final ApplicantJpaRepository applicantJpaRepository;

    public PostgreSQLApplicantRepository(ApplicantJpaRepository applicantJpaRepository) {
        this.applicantJpaRepository = applicantJpaRepository;
    }

    @Override
    public Long createApplicant(ApplicantModel applicantModel) {
        return applicantJpaRepository
                .save(ApplicantMapper.toEntity(applicantModel))
                .getId();
    }

    @Override
    public ApplicantModel getApplicantById(Long id) {
        return applicantJpaRepository
                .findById(id)
                .map(ApplicantMapper::toModel)
                .orElse(null);
    }

    @Override
    public ApplicantModel getApplicantByEmail(String email) {
        var entity = applicantJpaRepository
                .findByEmail(email);
        return entity != null ? ApplicantMapper.toModel(entity) : null;
    }

    @Override
    public ApplicantModel updateApplicant(ApplicantModel applicantModel) {
        var entity = applicantJpaRepository
                .save(ApplicantMapper.toEntity(applicantModel));
        return ApplicantMapper.toModel(entity);
    }

    @Override
    public Boolean deleteApplicant(Long id) {
        return applicantJpaRepository.findById(id)
                .map(entity -> {
                    applicantJpaRepository.delete(entity);
                    return true;
                })
                .orElse(false);
    }
}
