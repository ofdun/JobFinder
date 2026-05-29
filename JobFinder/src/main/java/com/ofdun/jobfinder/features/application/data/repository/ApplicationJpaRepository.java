package com.ofdun.jobfinder.features.application.data.repository;

import com.ofdun.jobfinder.features.application.data.entity.ApplicationEntity;
import java.util.List;
import lombok.NonNull;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ApplicationJpaRepository extends JpaRepository<@NonNull ApplicationEntity, @NonNull Long> {
	List<ApplicationEntity> findByVacancyIdOrderByDateDesc(Long vacancyId);
}
