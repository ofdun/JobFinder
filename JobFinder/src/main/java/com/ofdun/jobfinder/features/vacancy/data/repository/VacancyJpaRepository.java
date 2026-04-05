package com.ofdun.jobfinder.features.vacancy.data.repository;

import com.ofdun.jobfinder.features.vacancy.data.entity.VacancyEntity;
import lombok.NonNull;
import org.springframework.data.jpa.repository.JpaRepository;

public interface VacancyJpaRepository
        extends JpaRepository<@NonNull VacancyEntity, @NonNull Long> {}
