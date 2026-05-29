package com.ofdun.jobfinder.features.language.data.postgres.repository;

import com.ofdun.jobfinder.features.language.data.postgres.mapper.LanguageMapper;
import com.ofdun.jobfinder.features.language.domain.model.LanguageModel;
import com.ofdun.jobfinder.features.language.domain.repository.LanguageRepository;
import java.util.List;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@ConditionalOnProperty(name = "app.persistence.type", havingValue = "postgres")
public class PostgreSQLLanguageRepository implements LanguageRepository {
    private final LanguageJpaRepository jpaRepository;

    @Override
    public Optional<LanguageModel> getLanguageById(Long id) {
        return jpaRepository.findById(id).map(LanguageMapper::toModel);
    }

    @Override
    public List<LanguageModel> getAllLanguages() {
        return jpaRepository.findAllByOrderByNameAsc().stream()
                .map(LanguageMapper::toModel)
                .toList();
    }
}
