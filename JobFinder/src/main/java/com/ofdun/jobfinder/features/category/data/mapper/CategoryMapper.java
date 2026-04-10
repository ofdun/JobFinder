package com.ofdun.jobfinder.features.category.data.mapper;

import com.ofdun.jobfinder.features.category.data.entity.CategoryEntity;
import com.ofdun.jobfinder.features.category.domain.model.CategoryModel;

public class CategoryMapper {
    public static CategoryModel toModel(CategoryEntity entity) {
        if (entity == null) {
            return null;
        }
        return new CategoryModel(entity.getId(), entity.getName());
    }

    public static CategoryEntity toEntity(CategoryModel model) {
        if (model == null) {
            return null;
        }
        return new CategoryEntity(model.getId(), model.getName());
    }
}
