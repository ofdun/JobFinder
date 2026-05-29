package com.ofdun.jobfinder.features.skill.data.mongo.repository;

import com.ofdun.jobfinder.features.skill.data.mongo.mapper.SkillMongoMapper;
import com.ofdun.jobfinder.features.skill.domain.model.SkillModel;
import com.ofdun.jobfinder.features.skill.domain.repository.SkillRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;

@Component
@RequiredArgsConstructor
@ConditionalOnProperty(name = "app.persistence.type", havingValue = "mongo")
public class MongoSkillRepository implements SkillRepository {
    private final MongoSkillCRUDRepository mongoRepository;

    @Override
    public Optional<SkillModel> getSkillById(Long id) {
        return mongoRepository.findById(id).map(SkillMongoMapper::toModel);
    }

    @Override
    public List<SkillModel> getAllSkills() {
        return mongoRepository.findAllByOrderByNameAsc().stream()
                .map(SkillMongoMapper::toModel)
                .toList();
    }
}

