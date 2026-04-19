package com.ofdun.jobfinder.features.vacancy.api.controller;

import com.ofdun.jobfinder.common.api.dto.OffsetPaginationParams;
import com.ofdun.jobfinder.common.api.dto.PageResponse;
import com.ofdun.jobfinder.common.domain.model.OffsetPagination;
import com.ofdun.jobfinder.features.language.domain.model.LanguageModel;
import com.ofdun.jobfinder.features.language.domain.service.LanguageService;
import com.ofdun.jobfinder.features.location.domain.service.LocationService;
import com.ofdun.jobfinder.features.skill.domain.model.SkillModel;
import com.ofdun.jobfinder.features.skill.domain.service.SkillService;
import com.ofdun.jobfinder.features.vacancy.api.dto.VacancyRequest;
import com.ofdun.jobfinder.features.vacancy.api.dto.VacancyResponse;
import com.ofdun.jobfinder.features.vacancy.api.mapper.VacancyApiMapper;
import com.ofdun.jobfinder.features.vacancy.domain.model.VacancyModel;
import com.ofdun.jobfinder.features.vacancy.domain.model.VacancySearchFilter;
import com.ofdun.jobfinder.features.vacancy.domain.service.VacancyService;
import com.ofdun.jobfinder.features.vacancy.enums.EmploymentType;
import com.ofdun.jobfinder.features.vacancy.enums.JobFormat;
import com.ofdun.jobfinder.features.vacancy.enums.PaymentFrequency;
import jakarta.validation.Valid;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

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
    @PreAuthorize(
            "@sec.isEmployer(authentication) && @sec.isSelf(authentication, #request.employerId)")
    public ResponseEntity<@NonNull Long> createVacancy(@Valid @RequestBody VacancyRequest request) {
        VacancyModel model = mapper.toModel(request);
        Long id = vacancyService.createVacancy(model);
        return ResponseEntity.status(HttpStatus.CREATED).body(id);
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
        return ResponseEntity.ok(mapToResponse(vacancyService.updateVacancy(model)));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("@sec.employerOwnsVacancy(authentication, #id)")
    public ResponseEntity<@NonNull Void> deleteVacancy(@PathVariable Long id) {
        vacancyService.deleteVacancy(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/search")
    public ResponseEntity<PageResponse<VacancyResponse>> searchVacancies(
            @RequestParam(required = false) String q,
            @RequestParam(required = false) Long employerId,
            @RequestParam(required = false) Long locationId,
            @RequestParam(required = false) BigDecimal salaryMin,
            @RequestParam(required = false) BigDecimal salaryMax,
            @RequestParam(required = false) PaymentFrequency paymentFrequency,
            @RequestParam(required = false) EmploymentType employmentType,
            @RequestParam(required = false) JobFormat workFormat,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
                    Date publicationDateFrom,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
                    Date publicationDateTo,
            @RequestParam(required = false) List<Long> skillIds,
            @RequestParam(required = false) List<Long> languageIds,
            @Valid @ModelAttribute OffsetPaginationParams pagination) {

        var filter =
                new VacancySearchFilter(
                        q,
                        employerId,
                        locationId,
                        salaryMin,
                        salaryMax,
                        paymentFrequency,
                        employmentType,
                        workFormat,
                        publicationDateFrom,
                        publicationDateTo,
                        skillIds,
                        languageIds);

        var p =
                OffsetPagination.builder()
                        .limit(pagination.getLimit())
                        .offset(pagination.getOffset())
                        .sortBy(pagination.getSortBy())
                        .sortDesc(pagination.isSortDesc())
                        .build();

        var result = vacancyService.searchVacancies(filter, p);

        var items = result.getItems().stream().map(this::mapToResponse).toList();

        return ResponseEntity.ok(
                new PageResponse<>(
                        items,
                        result.getPage(),
                        result.getSize(),
                        result.getTotalElements(),
                        result.getTotalPages()));
    }

    private VacancyResponse mapToResponse(VacancyModel model) {
        var location = locationService.getLocationById(model.getLocationId());
        List<LanguageModel> languages = new ArrayList<>();

        if (model.getLanguageIds() != null) {
            for (Long languageId : model.getLanguageIds()) {
                languages.add(languageService.getLanguageById(languageId));
            }
        }

        List<SkillModel> skills = new ArrayList<>();
        if (model.getSkillIds() != null) {
            for (Long skillId : model.getSkillIds()) {
                skills.add(skillService.getSkillById(skillId));
            }
        }

        return mapper.toResponse(model, location, skills, languages);
    }
}
