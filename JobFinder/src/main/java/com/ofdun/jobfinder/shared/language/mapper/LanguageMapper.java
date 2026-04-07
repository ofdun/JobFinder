package com.ofdun.jobfinder.shared.language.mapper;

import com.ofdun.jobfinder.shared.language.entity.LanguageEntity;
import com.ofdun.jobfinder.shared.language.model.LanguageModel;

public class LanguageMapper {
    public static LanguageModel toModel(LanguageEntity entity) {
        if (entity == null) {
            return null;
        }
        return new LanguageModel(entity.getId(), entity.getName(), entity.getProficiencyLevel());
    }

    public static LanguageEntity toEntity(LanguageModel model) {
        if (model == null) {
            return null;
        }
        return new LanguageEntity(model.getId(), model.getName(), model.getProficiencyLevel());
    }
}
