package com.ofdun.jobfinder.features.skill.data.mongo.repository;

import com.ofdun.jobfinder.features.skill.data.mongo.entity.SkillDocument;
import java.util.List;
import lombok.NonNull;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface MongoSkillCRUDRepository extends MongoRepository<@NonNull SkillDocument, @NonNull Long> {
    List<SkillDocument> findAllByOrderByNameAsc();
}

