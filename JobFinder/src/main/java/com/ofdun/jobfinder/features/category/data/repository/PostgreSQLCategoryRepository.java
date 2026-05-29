package com.ofdun.jobfinder.features.category.data.repository;

import com.ofdun.jobfinder.features.category.data.mapper.CategoryMapper;
import com.ofdun.jobfinder.features.category.domain.model.CategoryModel;
import com.ofdun.jobfinder.features.category.domain.repository.CategoryRepository;
import java.util.List;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class PostgreSQLCategoryRepository implements CategoryRepository {
    private final CategoryJpaRepository categoryJpaRepository;

    @Override
    public Optional<CategoryModel> getCategoryById(Long id) {
        return categoryJpaRepository.findById(id).map(CategoryMapper::toModel);
    }

    @Override
    public List<CategoryModel> getAllCategories() {
        return categoryJpaRepository.findAllByOrderByNameAsc().stream()
                .map(CategoryMapper::toModel)
                .toList();
    }
}
