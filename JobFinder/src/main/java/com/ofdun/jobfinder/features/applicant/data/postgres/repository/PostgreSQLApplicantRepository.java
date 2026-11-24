package com.ofdun.jobfinder.features.applicant.data.postgres.repository;

import com.ofdun.jobfinder.features.applicant.data.postgres.mapper.ApplicantPostgreSQLMapper;
import com.ofdun.jobfinder.features.applicant.domain.model.ApplicantModel;
import com.ofdun.jobfinder.features.applicant.domain.repository.ApplicantRepository;
import io.micrometer.observation.annotation.Observed;
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
    @Observed(
            name = "jobfinder.applicant.repository.createApplicant",
            contextualName = "PostgreSQL: создание соискателя",
            lowCardinalityKeyValues = {
                "feature",
                "applicant",
                "layer",
                "repository",
                "storage",
                "PostgreSQL"
            })
    public Long createApplicant(ApplicantModel applicantModel) {
        return applicantJpaRepository
                .save(ApplicantPostgreSQLMapper.toEntity(applicantModel))
                .getId();
    }

    @Override
    @Observed(
            name = "jobfinder.applicant.repository.getApplicantById",
            contextualName = "PostgreSQL: получение соискателя по ID",
            lowCardinalityKeyValues = {
                "feature",
                "applicant",
                "layer",
                "repository",
                "storage",
                "PostgreSQL"
            })
    public Optional<ApplicantModel> getApplicantById(Long id) {
        return applicantJpaRepository.findById(id).map(ApplicantPostgreSQLMapper::toModel);
    }

    @Override
    @Observed(
            name = "jobfinder.applicant.repository.getApplicantByEmail",
            contextualName = "PostgreSQL: получение соискателя по почте",
            lowCardinalityKeyValues = {
                "feature",
                "applicant",
                "layer",
                "repository",
                "storage",
                "PostgreSQL"
            })
    public Optional<ApplicantModel> getApplicantByEmail(String email) {
        return applicantJpaRepository.findByEmail(email).map(ApplicantPostgreSQLMapper::toModel);
    }

    @Override
    @Observed(
            name = "jobfinder.applicant.repository.updateApplicant",
            contextualName = "PostgreSQL: обновление соискателя",
            lowCardinalityKeyValues = {
                "feature",
                "applicant",
                "layer",
                "repository",
                "storage",
                "PostgreSQL"
            })
    public ApplicantModel updateApplicant(ApplicantModel applicantModel) {
        var entity =
                applicantJpaRepository.save(ApplicantPostgreSQLMapper.toEntity(applicantModel));
        return ApplicantPostgreSQLMapper.toModel(entity);
    }

    @Override
    @Observed(
            name = "jobfinder.applicant.repository.deleteApplicant",
            contextualName = "PostgreSQL: удаление соискателя",
            lowCardinalityKeyValues = {
                "feature",
                "applicant",
                "layer",
                "repository",
                "storage",
                "PostgreSQL"
            })
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
