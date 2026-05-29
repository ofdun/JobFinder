package com.ofdun.jobfinder.features.language.domain.service;

import com.ofdun.jobfinder.features.language.domain.model.LanguageModel;
import java.util.List;

public interface LanguageService {
    LanguageModel getLanguageById(Long id);

    List<LanguageModel> getAllLanguages();
}
