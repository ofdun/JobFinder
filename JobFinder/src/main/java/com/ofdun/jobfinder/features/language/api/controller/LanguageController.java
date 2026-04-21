package com.ofdun.jobfinder.features.language.api.controller;

import com.ofdun.jobfinder.features.language.api.dto.LanguageDto;
import com.ofdun.jobfinder.features.language.api.mapper.LanguageApiMapper;
import com.ofdun.jobfinder.features.language.domain.service.LanguageService;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/languages")
@RequiredArgsConstructor
public class LanguageController {
    private final LanguageService languageService;

    @GetMapping
    public ResponseEntity<List<LanguageDto>> getLanguages() {
        var languages = languageService.getAllLanguages().stream().map(LanguageApiMapper::toDto).toList();
        return ResponseEntity.ok(languages);
    }
}

