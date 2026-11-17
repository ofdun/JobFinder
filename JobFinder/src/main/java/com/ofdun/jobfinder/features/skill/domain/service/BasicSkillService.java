package com.ofdun.jobfinder.features.skill.domain.service;

import com.ofdun.jobfinder.features.skill.domain.model.SkillModel;
import com.ofdun.jobfinder.features.skill.domain.repository.SkillRepository;
import com.ofdun.jobfinder.features.skill.exception.SkillNotFoundException;
import io.micrometer.observation.annotation.Observed;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class BasicSkillService implements SkillService {
    private final SkillRepository skillRepository;

    @Override
    @Observed(
            name = "jobfinder.skill.service.getSkillById",
            contextualName = "Получение навыка по ID",
            lowCardinalityKeyValues = {"feature", "skill", "layer", "service"})
    public SkillModel getSkillById(Long id) {
        return skillRepository.getSkillById(id).orElseThrow(() -> new SkillNotFoundException(id));
    }

    @Override
    @Cacheable(cacheNames = "skills")
    @Observed(
            name = "jobfinder.skill.service.getAllSkills",
            contextualName = "Получение списка навыков",
            lowCardinalityKeyValues = {"feature", "skill", "layer", "service"})
    public List<SkillModel> getAllSkills() {
        return skillRepository.getAllSkills();
    }
}
