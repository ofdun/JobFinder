package com.ofdun.jobfinder.shared.skill.mapper;

import com.ofdun.jobfinder.shared.skill.entity.SkillEntity;
import com.ofdun.jobfinder.shared.skill.model.SkillModel;

public class SkillMapper {
    public static SkillModel toModel(SkillEntity entity) {
        if (entity == null) {
            return null;
        }
        return new SkillModel(entity.getId(), entity.getName());
    }

    public static SkillEntity toEntity(SkillModel model) {
        if (model == null) {
            return null;
        }
        return new SkillEntity(model.getId(), model.getName());
    }
}
