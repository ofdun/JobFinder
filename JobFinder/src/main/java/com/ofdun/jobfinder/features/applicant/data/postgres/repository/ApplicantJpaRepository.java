package com.ofdun.jobfinder.features.applicant.data.postgres.repository;

import com.ofdun.jobfinder.features.applicant.data.postgres.entity.ApplicantEntity;
import java.util.Optional;
import lombok.NonNull;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ApplicantJpaRepository
        extends JpaRepository<@NonNull ApplicantEntity, @NonNull Long> {
    Optional<ApplicantEntity> findByEmail(String email);
}
