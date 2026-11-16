package com.ofdun.jobfinder.features.category.data.postgres.repository;

import com.ofdun.jobfinder.features.category.data.postgres.mapper.CategoryMapper;
import com.ofdun.jobfinder.features.category.domain.model.CategoryModel;
import com.ofdun.jobfinder.features.category.domain.repository.CategoryRepository;
import io.micrometer.observation.annotation.Observed;
import java.util.List;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@ConditionalOnProperty(name = "app.persistence.type", havingValue = "postgres")
public class PostgreSQLCategoryRepository implements CategoryRepository {
    private final CategoryJpaRepository categoryJpaRepository;

    @Override
    @Observed(
            name = "jobfinder.category.repository.getCategoryById",
            contextualName = "PostgreSQL: получение категории по ID",
            lowCardinalityKeyValues = {
                "feature",
                "category",
                "layer",
                "repository",
                "storage",
                "PostgreSQL"
            })
    public Optional<CategoryModel> getCategoryById(Long id) {
        return categoryJpaRepository.findById(id).map(CategoryMapper::toModel);
    }

    @Override
    @Observed(
            name = "jobfinder.category.repository.getAllCategories",
            contextualName = "PostgreSQL: получение списка категорий",
            lowCardinalityKeyValues = {
                "feature",
                "category",
                "layer",
                "repository",
                "storage",
                "PostgreSQL"
            })
    public List<CategoryModel> getAllCategories() {
        return categoryJpaRepository.findAllByOrderByNameAsc().stream()
                .map(CategoryMapper::toModel)
                .toList();
    }
}
