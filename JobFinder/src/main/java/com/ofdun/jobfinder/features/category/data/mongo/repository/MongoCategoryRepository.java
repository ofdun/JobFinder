package com.ofdun.jobfinder.features.category.data.mongo.repository;

import com.ofdun.jobfinder.features.category.data.mongo.mapper.CategoryMongoMapper;
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
@ConditionalOnProperty(name = "app.persistence.type", havingValue = "mongo")
public class MongoCategoryRepository implements CategoryRepository {
    private final MongoCategoryCRUDRepository mongoRepository;

    @Override
    @Observed(
            name = "jobfinder.category.repository.getCategoryById",
            contextualName = "MongoDB: получение категории по ID",
            lowCardinalityKeyValues = {
                "feature",
                "category",
                "layer",
                "repository",
                "storage",
                "MongoDB"
            })
    public Optional<CategoryModel> getCategoryById(Long id) {
        return mongoRepository.findById(id).map(CategoryMongoMapper::toModel);
    }

    @Override
    @Observed(
            name = "jobfinder.category.repository.getAllCategories",
            contextualName = "MongoDB: получение списка категорий",
            lowCardinalityKeyValues = {
                "feature",
                "category",
                "layer",
                "repository",
                "storage",
                "MongoDB"
            })
    public List<CategoryModel> getAllCategories() {
        return mongoRepository.findAllByOrderByNameAsc().stream()
                .map(CategoryMongoMapper::toModel)
                .toList();
    }
}
