package com.ofdun.jobfinder.features.skill.domain.repository;

import com.ofdun.jobfinder.features.skill.domain.model.SkillModel;
import java.util.Optional;

public interface SkillRepository {
    Optional<SkillModel> getSkillById(Long id);
}
