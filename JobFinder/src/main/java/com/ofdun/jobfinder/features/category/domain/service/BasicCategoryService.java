package com.ofdun.jobfinder.features.category.domain.service;

import com.ofdun.jobfinder.features.category.domain.model.CategoryModel;
import com.ofdun.jobfinder.features.category.domain.repository.CategoryRepository;
import com.ofdun.jobfinder.features.category.exception.CategoryNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@RequiredArgsConstructor
@Service
public class BasicCategoryService implements CategoryService{
    private final CategoryRepository categoryRepository;

    @Override
    public CategoryModel getCategoryById(Long id) {
        return categoryRepository.getCategoryById(id)
                .orElseThrow(() -> new CategoryNotFoundException(id));
    }
}
