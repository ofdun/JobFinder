package com.ofdun.jobfinder.features.skill.domain.service;

import com.ofdun.jobfinder.features.skill.domain.model.SkillModel;
import com.ofdun.jobfinder.features.skill.domain.repository.SkillRepository;
import java.util.List;
import com.ofdun.jobfinder.features.skill.exception.SkillNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class BasicSkillService implements SkillService {
    private final SkillRepository skillRepository;

    @Override
    public SkillModel getSkillById(Long id) {
        return skillRepository.getSkillById(id).orElseThrow(() -> new SkillNotFoundException(id));
    }

    @Override
    @Cacheable(cacheNames = "skills")
    public List<SkillModel> getAllSkills() {
        return skillRepository.getAllSkills();
    }
}
