package com.ofdun.jobfinder.features.language.data.mongo.repository;

import com.ofdun.jobfinder.features.language.data.mongo.mapper.LanguageMongoMapper;
import com.ofdun.jobfinder.features.language.domain.model.LanguageModel;
import com.ofdun.jobfinder.features.language.domain.repository.LanguageRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;

@Component
@RequiredArgsConstructor
@ConditionalOnProperty(name = "app.persistence.type", havingValue = "mongo")
public class MongoLanguageRepository implements LanguageRepository {
    private final MongoLanguageCRUDRepository mongoRepository;

    @Override
    public Optional<LanguageModel> getLanguageById(Long id) {
        return mongoRepository.findById(id).map(LanguageMongoMapper::toModel);
    }

    @Override
    public List<LanguageModel> getAllLanguages() {
        return mongoRepository.findAllByOrderByNameAsc().stream()
                .map(LanguageMongoMapper::toModel)
                .toList();
    }
}

