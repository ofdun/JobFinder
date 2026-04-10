package com.ofdun.jobfinder.features.category.data.repository;

import com.ofdun.jobfinder.features.category.data.mapper.CategoryMapper;
import com.ofdun.jobfinder.features.category.domain.model.CategoryModel;
import com.ofdun.jobfinder.features.category.domain.repository.CategoryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
@RequiredArgsConstructor
public class PostgreSQLCategoryRepository implements CategoryRepository {
    private final CategoryJpaRepository categoryJpaRepository;

    @Override
    public Optional<CategoryModel> getCategoryById(Long id) {
        return categoryJpaRepository.findById(id)
                .map(CategoryMapper::toModel);
    }
}
