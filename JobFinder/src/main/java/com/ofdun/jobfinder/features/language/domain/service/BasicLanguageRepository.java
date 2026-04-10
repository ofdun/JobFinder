package com.ofdun.jobfinder.features.language.domain.service;

import com.ofdun.jobfinder.features.language.domain.model.LanguageModel;
import com.ofdun.jobfinder.features.language.domain.repository.LanguageRepository;
import com.ofdun.jobfinder.features.language.exception.LanguageNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class BasicLanguageRepository implements LanguageService {
    private final LanguageRepository languageRepository;

    @Override
    public LanguageModel getLanguageById(Long id) {
        return languageRepository.getLanguageById(id)
                .orElseThrow(() -> new LanguageNotFoundException(id));
    }
}
