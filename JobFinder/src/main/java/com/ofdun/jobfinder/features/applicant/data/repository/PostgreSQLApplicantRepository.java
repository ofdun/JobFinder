package com.ofdun.jobfinder.features.applicant.data.repository;

import com.ofdun.jobfinder.features.applicant.data.mapper.ApplicantMapper;
import com.ofdun.jobfinder.features.applicant.domain.model.ApplicantModel;
import com.ofdun.jobfinder.features.applicant.domain.repository.ApplicantRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
@RequiredArgsConstructor
public class PostgreSQLApplicantRepository implements ApplicantRepository {
    private final ApplicantJpaRepository applicantJpaRepository;

    @Override
    public Long createApplicant(ApplicantModel applicantModel) {
        return applicantJpaRepository.save(ApplicantMapper.toEntity(applicantModel)).getId();
    }

    @Override
    public Optional<ApplicantModel> getApplicantById(Long id) {
        return applicantJpaRepository.findById(id).map(ApplicantMapper::toModel);
    }

    @Override
    public Optional<ApplicantModel> getApplicantByEmail(String email) {
        return applicantJpaRepository.findByEmail(email).map(ApplicantMapper::toModel);
    }

    @Override
    public ApplicantModel updateApplicant(ApplicantModel applicantModel) {
        var entity = applicantJpaRepository.save(ApplicantMapper.toEntity(applicantModel));
        return ApplicantMapper.toModel(entity);
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
