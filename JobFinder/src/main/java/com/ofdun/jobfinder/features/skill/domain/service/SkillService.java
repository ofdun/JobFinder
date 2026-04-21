package com.ofdun.jobfinder.features.skill.domain.service;

import com.ofdun.jobfinder.features.skill.domain.model.SkillModel;
import java.util.List;

public interface SkillService {
    SkillModel getSkillById(Long id);

    List<SkillModel> getAllSkills();
}
