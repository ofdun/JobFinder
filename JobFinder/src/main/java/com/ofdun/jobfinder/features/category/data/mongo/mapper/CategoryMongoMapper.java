package com.ofdun.jobfinder.features.category.data.mongo.mapper;

import com.ofdun.jobfinder.features.category.data.mongo.entity.CategoryDocument;
import com.ofdun.jobfinder.features.category.domain.model.CategoryModel;

public class CategoryMongoMapper {
    public static CategoryModel toModel(CategoryDocument entity) {
        if (entity == null) {
            return null;
        }
        return new CategoryModel(entity.getId(), entity.getName());
    }

    public static CategoryDocument toEntity(CategoryModel model) {
        if (model == null) {
            return null;
        }
        return new CategoryDocument(model.getId(), model.getName());
    }
}

