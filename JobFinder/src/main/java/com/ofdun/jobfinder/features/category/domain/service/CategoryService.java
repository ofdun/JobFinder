package com.ofdun.jobfinder.features.category.domain.service;

import com.ofdun.jobfinder.features.category.domain.model.CategoryModel;
import java.util.List;

public interface CategoryService {
    CategoryModel getCategoryById(Long id);

    List<CategoryModel> getAllCategories();
}
