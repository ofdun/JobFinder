package com.ofdun.jobfinder.features.skill.data.mongo.mapper;

import com.ofdun.jobfinder.features.skill.data.mongo.entity.SkillDocument;
import com.ofdun.jobfinder.features.skill.domain.model.SkillModel;

public class SkillMongoMapper {
    public static SkillModel toModel(SkillDocument entity) {
        if (entity == null) {
            return null;
        }
        return new SkillModel(entity.getId(), entity.getName());
    }

    public static SkillDocument toEntity(SkillModel model) {
        if (model == null) {
            return null;
        }
        return new SkillDocument(model.getId(), model.getName());
    }
}

