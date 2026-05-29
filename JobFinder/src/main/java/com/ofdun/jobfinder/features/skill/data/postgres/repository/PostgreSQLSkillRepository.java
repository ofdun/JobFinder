package com.ofdun.jobfinder.features.skill.data.postgres.repository;

import com.ofdun.jobfinder.features.skill.data.postgres.mapper.SkillMapper;
import com.ofdun.jobfinder.features.skill.domain.model.SkillModel;
import com.ofdun.jobfinder.features.skill.domain.repository.SkillRepository;
import java.util.List;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@ConditionalOnProperty(name = "app.persistence.type", havingValue = "postgres")
public class PostgreSQLSkillRepository implements SkillRepository {
    private final SkillJpaRepository skillJpaRepository;

    @Override
    public Optional<SkillModel> getSkillById(Long id) {
        return skillJpaRepository.findById(id).map(SkillMapper::toModel);
    }

    @Override
    public List<SkillModel> getAllSkills() {
        return skillJpaRepository.findAllByOrderByNameAsc().stream()
                .map(SkillMapper::toModel)
                .toList();
    }
}
