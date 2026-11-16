package com.ofdun.jobfinder.features.skill.data.mongo.repository;

import com.ofdun.jobfinder.features.skill.data.mongo.mapper.SkillMongoMapper;
import com.ofdun.jobfinder.features.skill.domain.model.SkillModel;
import com.ofdun.jobfinder.features.skill.domain.repository.SkillRepository;
import io.micrometer.observation.annotation.Observed;
import java.util.List;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@ConditionalOnProperty(name = "app.persistence.type", havingValue = "mongo")
public class MongoSkillRepository implements SkillRepository {
    private final MongoSkillCRUDRepository mongoRepository;

    @Override
    @Observed(
            name = "jobfinder.skill.repository.getSkillById",
            contextualName = "MongoDB: получение навыка по ID",
            lowCardinalityKeyValues = {
                "feature",
                "skill",
                "layer",
                "repository",
                "storage",
                "MongoDB"
            })
    public Optional<SkillModel> getSkillById(Long id) {
        return mongoRepository.findById(id).map(SkillMongoMapper::toModel);
    }

    @Override
    @Observed(
            name = "jobfinder.skill.repository.getAllSkills",
            contextualName = "MongoDB: получение списка навыков",
            lowCardinalityKeyValues = {
                "feature",
                "skill",
                "layer",
                "repository",
                "storage",
                "MongoDB"
            })
    public List<SkillModel> getAllSkills() {
        return mongoRepository.findAllByOrderByNameAsc().stream()
                .map(SkillMongoMapper::toModel)
                .toList();
    }
}
