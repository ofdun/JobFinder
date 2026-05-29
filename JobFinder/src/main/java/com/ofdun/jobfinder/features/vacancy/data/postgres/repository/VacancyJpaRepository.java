package com.ofdun.jobfinder.features.vacancy.data.postgres.repository;

import com.ofdun.jobfinder.features.vacancy.data.postgres.entity.VacancyEntity;
import lombok.NonNull;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

public interface VacancyJpaRepository
        extends JpaRepository<@NonNull VacancyEntity, @NonNull Long>,
                JpaSpecificationExecutor<VacancyEntity> {}
