package com.ofdun.jobfinder.features.category.data.mongo.repository;

import com.ofdun.jobfinder.features.category.data.mongo.entity.CategoryDocument;
import java.util.List;
import lombok.NonNull;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface MongoCategoryCRUDRepository
        extends MongoRepository<@NonNull CategoryDocument, @NonNull Long> {
    List<CategoryDocument> findAllByOrderByNameAsc();
}

