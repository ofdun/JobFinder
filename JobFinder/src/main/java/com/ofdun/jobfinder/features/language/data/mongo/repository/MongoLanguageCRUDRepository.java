package com.ofdun.jobfinder.features.language.data.mongo.repository;

import com.ofdun.jobfinder.features.language.data.mongo.entity.LanguageDocument;
import java.util.List;
import lombok.NonNull;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface MongoLanguageCRUDRepository
        extends MongoRepository<@NonNull LanguageDocument, @NonNull Long> {
    List<LanguageDocument> findAllByOrderByNameAsc();
}

