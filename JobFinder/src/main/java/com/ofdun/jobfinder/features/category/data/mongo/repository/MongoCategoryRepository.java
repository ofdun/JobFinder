package com.ofdun.jobfinder.features.category.data.mongo.repository;

import com.ofdun.jobfinder.features.category.data.mongo.mapper.CategoryMongoMapper;
import com.ofdun.jobfinder.features.category.domain.model.CategoryModel;
import com.ofdun.jobfinder.features.category.domain.repository.CategoryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;

@Component
@RequiredArgsConstructor
@ConditionalOnProperty(name = "app.persistence.type", havingValue = "mongo")
public class MongoCategoryRepository implements CategoryRepository {
    private final MongoCategoryCRUDRepository mongoRepository;

    @Override
    public Optional<CategoryModel> getCategoryById(Long id) {
        return mongoRepository.findById(id).map(CategoryMongoMapper::toModel);
    }

    @Override
    public List<CategoryModel> getAllCategories() {
        return mongoRepository.findAllByOrderByNameAsc().stream()
                .map(CategoryMongoMapper::toModel)
                .toList();
    }
}

