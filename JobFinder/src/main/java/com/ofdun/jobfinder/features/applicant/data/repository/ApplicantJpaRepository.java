package com.ofdun.jobfinder.features.applicant.data.repository;

import com.ofdun.jobfinder.features.applicant.data.entity.ApplicantEntity;
import lombok.NonNull;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ApplicantJpaRepository extends JpaRepository<@NonNull ApplicantEntity, @NonNull Long> {
    ApplicantEntity findByEmail(String email);
}
