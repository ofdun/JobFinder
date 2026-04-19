package com.ofdun.jobfinder.features.vacancy.data.repository;

import com.ofdun.jobfinder.features.vacancy.data.entity.VacancyEntity;
import lombok.NonNull;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

public interface VacancyJpaRepository
        extends JpaRepository<@NonNull VacancyEntity, @NonNull Long>,
                JpaSpecificationExecutor<VacancyEntity> {}
