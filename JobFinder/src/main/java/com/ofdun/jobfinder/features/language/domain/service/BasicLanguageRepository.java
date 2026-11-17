package com.ofdun.jobfinder.features.language.domain.service;

import com.ofdun.jobfinder.features.language.domain.model.LanguageModel;
import com.ofdun.jobfinder.features.language.domain.repository.LanguageRepository;
import com.ofdun.jobfinder.features.language.exception.LanguageNotFoundException;
import io.micrometer.observation.annotation.Observed;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class BasicLanguageRepository implements LanguageService {
    private final LanguageRepository languageRepository;

    @Override
    @Observed(
            name = "jobfinder.language.service.getLanguageById",
            contextualName = "Получение языка по ID",
            lowCardinalityKeyValues = {"feature", "language", "layer", "service"})
    public LanguageModel getLanguageById(Long id) {
        return languageRepository
                .getLanguageById(id)
                .orElseThrow(() -> new LanguageNotFoundException(id));
    }

    @Override
    @Cacheable(cacheNames = "languages")
    @Observed(
            name = "jobfinder.language.service.getAllLanguages",
            contextualName = "Получение списка языков",
            lowCardinalityKeyValues = {"feature", "language", "layer", "service"})
    public List<LanguageModel> getAllLanguages() {
        return languageRepository.getAllLanguages();
    }
}
