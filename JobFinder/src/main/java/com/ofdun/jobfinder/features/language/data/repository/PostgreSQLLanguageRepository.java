package com.ofdun.jobfinder.features.language.data.repository;

import com.ofdun.jobfinder.features.language.data.mapper.LanguageMapper;
import com.ofdun.jobfinder.features.language.domain.model.LanguageModel;
import com.ofdun.jobfinder.features.language.domain.repository.LanguageRepository;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class PostgreSQLLanguageRepository implements LanguageRepository {
    private final LanguageJpaRepository jpaRepository;

    @Override
    public Optional<LanguageModel> getLanguageById(Long id) {
        return jpaRepository.findById(id).map(LanguageMapper::toModel);
    }
}
