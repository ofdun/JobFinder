package com.ofdun.jobfinder.features.employer.data.postgres.repository;

import com.ofdun.jobfinder.features.employer.data.postgres.entity.EmployerEntity;
import java.util.Optional;
import lombok.NonNull;
import org.springframework.data.jpa.repository.JpaRepository;

public interface EmployerJpaRepository
        extends JpaRepository<@NonNull EmployerEntity, @NonNull Long> {
    Optional<EmployerEntity> findByEmail(String email);
}
