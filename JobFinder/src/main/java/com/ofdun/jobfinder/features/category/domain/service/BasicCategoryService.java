package com.ofdun.jobfinder.features.category.domain.service;

import com.ofdun.jobfinder.features.category.domain.model.CategoryModel;
import com.ofdun.jobfinder.features.category.domain.repository.CategoryRepository;
import com.ofdun.jobfinder.features.category.exception.CategoryNotFoundException;
import io.micrometer.observation.annotation.Observed;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

@RequiredArgsConstructor
@Service
public class BasicCategoryService implements CategoryService {
    private final CategoryRepository categoryRepository;

    @Override
    @Observed(
            name = "jobfinder.category.service.getCategoryById",
            contextualName = "Получение категории по ID",
            lowCardinalityKeyValues = {"feature", "category", "layer", "service"})
    public CategoryModel getCategoryById(Long id) {
        return categoryRepository
                .getCategoryById(id)
                .orElseThrow(() -> new CategoryNotFoundException(id));
    }

    @Override
    @Cacheable(cacheNames = "categories")
    @Observed(
            name = "jobfinder.category.service.getAllCategories",
            contextualName = "Получение списка категорий",
            lowCardinalityKeyValues = {"feature", "category", "layer", "service"})
    public List<CategoryModel> getAllCategories() {
        return categoryRepository.getAllCategories();
    }
}
