package com.ofdun.jobfinder.features.language.data.mongo.repository;

import com.ofdun.jobfinder.features.language.data.mongo.mapper.LanguageMongoMapper;
import com.ofdun.jobfinder.features.language.domain.model.LanguageModel;
import com.ofdun.jobfinder.features.language.domain.repository.LanguageRepository;
import io.micrometer.observation.annotation.Observed;
import java.util.List;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@ConditionalOnProperty(name = "app.persistence.type", havingValue = "mongo")
public class MongoLanguageRepository implements LanguageRepository {
    private final MongoLanguageCRUDRepository mongoRepository;

    @Override
    @Observed(
            name = "jobfinder.language.repository.getLanguageById",
            contextualName = "MongoDB: получение языка по ID",
            lowCardinalityKeyValues = {
                "feature",
                "language",
                "layer",
                "repository",
                "storage",
                "MongoDB"
            })
    public Optional<LanguageModel> getLanguageById(Long id) {
        return mongoRepository.findById(id).map(LanguageMongoMapper::toModel);
    }

    @Override
    @Observed(
            name = "jobfinder.language.repository.getAllLanguages",
            contextualName = "MongoDB: получение списка языков",
            lowCardinalityKeyValues = {
                "feature",
                "language",
                "layer",
                "repository",
                "storage",
                "MongoDB"
            })
    public List<LanguageModel> getAllLanguages() {
        return mongoRepository.findAllByOrderByNameAsc().stream()
                .map(LanguageMongoMapper::toModel)
                .toList();
    }
}
