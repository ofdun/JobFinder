package com.ofdun.jobfinder.features.language.api.mapper;

import com.ofdun.jobfinder.features.language.api.dto.LanguageDto;
import com.ofdun.jobfinder.features.language.domain.model.LanguageModel;

public class LanguageApiMapper {
    public static LanguageDto toDto(LanguageModel model) {
        if (model == null) {
            return null;
        }
        return new LanguageDto(model.getId(), model.getName(), model.getProficiencyLevel());
    }
    public static LanguageModel toModel(LanguageDto dto) {
        if (dto == null) {
            return null;
        }
        return new LanguageModel(dto.getId(), dto.getName(), dto.getProficiencyLevel());
    }
}
