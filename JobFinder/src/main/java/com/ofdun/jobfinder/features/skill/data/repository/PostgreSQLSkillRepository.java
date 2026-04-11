package com.ofdun.jobfinder.features.skill.data.repository;

import com.ofdun.jobfinder.features.skill.data.mapper.SkillMapper;
import com.ofdun.jobfinder.features.skill.domain.model.SkillModel;
import com.ofdun.jobfinder.features.skill.domain.repository.SkillRepository;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@RequiredArgsConstructor
@Component
public class PostgreSQLSkillRepository implements SkillRepository {
    private final SkillJpaRepository skillJpaRepository;

    @Override
    public Optional<SkillModel> getSkillById(Long id) {
        return skillJpaRepository.findById(id).map(SkillMapper::toModel);
    }
}
