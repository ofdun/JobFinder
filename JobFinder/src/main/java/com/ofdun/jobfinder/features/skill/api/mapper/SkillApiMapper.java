package com.ofdun.jobfinder.features.skill.api.mapper;

import com.ofdun.jobfinder.features.skill.api.dto.SkillDto;
import com.ofdun.jobfinder.features.skill.domain.model.SkillModel;

public class SkillApiMapper {
    public static SkillDto toDto(SkillModel model) {
        if (model == null) {
            return null;
        }
        return new SkillDto(model.getId(), model.getName());
    }

    public static SkillModel toModel(SkillDto dto) {
        if (dto == null) {
            return null;
        }
        return new SkillModel(dto.getId(), dto.getName());
    }
}
