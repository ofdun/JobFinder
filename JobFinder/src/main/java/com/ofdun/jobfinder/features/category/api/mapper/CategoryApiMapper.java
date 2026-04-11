package com.ofdun.jobfinder.features.category.api.mapper;

import com.ofdun.jobfinder.features.category.api.dto.CategoryDto;
import com.ofdun.jobfinder.features.category.domain.model.CategoryModel;

public class CategoryApiMapper {
    public static CategoryDto toDto(CategoryModel model) {
        if (model == null) {
            return null;
        }
        return new CategoryDto(model.getId(), model.getName());
    }

    public static CategoryModel toModel(CategoryDto dto) {
        if (dto == null) {
            return null;
        }
        return new CategoryModel(dto.getId(), dto.getName());
    }
}
