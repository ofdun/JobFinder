package com.ofdun.jobfinder.features.category.domain.repository;

import com.ofdun.jobfinder.features.category.domain.model.CategoryModel;
import java.util.List;
import java.util.Optional;

public interface CategoryRepository {
    Optional<CategoryModel> getCategoryById(Long id);

    List<CategoryModel> getAllCategories();
}
