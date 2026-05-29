package com.ofdun.jobfinder.features.skill.data.postgres.repository;

import com.ofdun.jobfinder.features.skill.data.postgres.entity.SkillEntity;
import java.util.List;
import lombok.NonNull;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SkillJpaRepository extends JpaRepository<@NonNull SkillEntity, @NonNull Long> {
	List<SkillEntity> findAllByOrderByNameAsc();
}
