package com.ofdun.jobfinder.features.vacancy.data.postgres.repository;

import com.ofdun.jobfinder.features.vacancy.data.postgres.entity.DraftVacancyEntity;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface DraftVacancyJpaRepository extends JpaRepository<DraftVacancyEntity, Long> {
    List<DraftVacancyEntity> findAllByVacancyIdOrderByVersionTimestampDesc(Long vacancyId);
}

