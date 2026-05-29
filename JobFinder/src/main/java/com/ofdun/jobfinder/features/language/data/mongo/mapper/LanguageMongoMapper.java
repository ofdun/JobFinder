package com.ofdun.jobfinder.features.language.data.mongo.mapper;

import com.ofdun.jobfinder.features.language.data.mongo.entity.LanguageDocument;
import com.ofdun.jobfinder.features.language.domain.model.LanguageModel;

public class LanguageMongoMapper {
    public static LanguageModel toModel(LanguageDocument entity) {
        if (entity == null) {
            return null;
        }
        return new LanguageModel(entity.getId(), entity.getName(), entity.getProficiencyLevel());
    }

    public static LanguageDocument toEntity(LanguageModel model) {
        if (model == null) {
            return null;
        }
        return new LanguageDocument(model.getId(), model.getName(), model.getProficiencyLevel());
    }
}

