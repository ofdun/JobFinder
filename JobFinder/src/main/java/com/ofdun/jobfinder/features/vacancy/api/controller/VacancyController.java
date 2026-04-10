package com.ofdun.jobfinder.features.vacancy.api.controller;

import com.ofdun.jobfinder.features.language.domain.model.LanguageModel;
import com.ofdun.jobfinder.features.language.domain.service.LanguageService;
import com.ofdun.jobfinder.features.location.domain.service.LocationService;
import com.ofdun.jobfinder.features.skill.domain.model.SkillModel;
import com.ofdun.jobfinder.features.skill.domain.service.SkillService;
import com.ofdun.jobfinder.features.vacancy.api.dto.VacancyRequest;
import com.ofdun.jobfinder.features.vacancy.api.dto.VacancyResponse;
import com.ofdun.jobfinder.features.vacancy.api.mapper.VacancyApiMapper;
import com.ofdun.jobfinder.features.vacancy.domain.model.VacancyModel;
import com.ofdun.jobfinder.features.vacancy.domain.service.VacancyService;
import com.ofdun.jobfinder.features.vacancy.exception.VacancyNotFoundException;
import jakarta.validation.Valid;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;

@RestController
@RequestMapping("/api/v1/vacancies")
@RequiredArgsConstructor
public class VacancyController {
    private final VacancyService vacancyService;
    private final LanguageService languageService;
    private final LocationService locationService;
    private final SkillService skillService;
    private final VacancyApiMapper mapper;

    @PostMapping
    @PreAuthorize("@sec.isEmployer(authentication) && @sec.isSelf(authentication, #request.employerId)")
    public ResponseEntity<@NonNull Long> createVacancy(@Valid @RequestBody VacancyRequest request) {
        return ResponseEntity.ok(vacancyService.createVacancy(mapper.toModel(request)));
    }

    @GetMapping("/{id}")
    public ResponseEntity<@NonNull VacancyResponse> getVacancy(@PathVariable Long id) {
        VacancyModel model = vacancyService.getVacancyById(id);
        return ResponseEntity.ok(mapToResponse(model));
    }

    @PutMapping("/{id}")
    @PreAuthorize("@sec.employerOwnsVacancy(authentication, #id)")
    public ResponseEntity<@NonNull VacancyResponse> updateVacancy(
            @PathVariable Long id, @Valid @RequestBody VacancyRequest request) {
        VacancyModel model = mapper.toModel(request);
        model.setId(id);
        return ResponseEntity.ok(mapToResponse(model));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("@sec.employerOwnsVacancy(authentication, #id)")
    public ResponseEntity<@NonNull Void> deleteVacancy(@PathVariable Long id) {
        vacancyService.deleteVacancy(id);
        return ResponseEntity.noContent().build();
    }

    private VacancyResponse mapToResponse(VacancyModel model) {
        var location = locationService.getLocationById(model.getLocationId());
        List<LanguageModel> languages = new ArrayList<>();
        for (Long languageId : model.getLanguageIds()) {
            languages.add(languageService.getLanguageById(languageId));
        }

        List<SkillModel> skills = new ArrayList<>();
        for (Long skillId : model.getSkillIds()) {
            skills.add(skillService.getSkillById(skillId));
        }

        return mapper.toResponse(model, location, skills, languages);
    }
}
