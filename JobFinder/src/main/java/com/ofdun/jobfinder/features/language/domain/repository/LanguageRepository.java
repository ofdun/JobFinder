package com.ofdun.jobfinder.features.language.domain.repository;

import com.ofdun.jobfinder.features.language.domain.model.LanguageModel;
import java.util.List;
import java.util.Optional;

public interface LanguageRepository {
    Optional<LanguageModel> getLanguageById(Long id);

    List<LanguageModel> getAllLanguages();
}
