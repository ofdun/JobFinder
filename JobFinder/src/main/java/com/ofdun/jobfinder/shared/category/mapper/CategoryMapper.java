package com.ofdun.jobfinder.shared.category.mapper;

import com.ofdun.jobfinder.shared.category.entity.CategoryEntity;
import com.ofdun.jobfinder.shared.category.model.CategoryModel;

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
