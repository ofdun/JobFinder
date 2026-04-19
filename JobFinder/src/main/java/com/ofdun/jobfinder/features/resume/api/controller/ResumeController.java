package com.ofdun.jobfinder.features.resume.api.controller;

import com.ofdun.jobfinder.common.api.dto.OffsetPaginationParams;
import com.ofdun.jobfinder.common.api.dto.PageResponse;
import com.ofdun.jobfinder.common.domain.model.OffsetPagination;
import com.ofdun.jobfinder.features.category.domain.service.CategoryService;
import com.ofdun.jobfinder.features.language.domain.model.LanguageModel;
import com.ofdun.jobfinder.features.language.domain.service.LanguageService;
import com.ofdun.jobfinder.features.resume.api.dto.ResumeRequest;
import com.ofdun.jobfinder.features.resume.api.dto.ResumeResponse;
import com.ofdun.jobfinder.features.resume.api.dto.ResumeUpdateRequest;
import com.ofdun.jobfinder.features.resume.api.mapper.ResumeApiMapper;
import com.ofdun.jobfinder.features.resume.domain.model.ResumeModel;
import com.ofdun.jobfinder.features.resume.domain.model.ResumeSearchFilter;
import com.ofdun.jobfinder.features.resume.domain.service.ResumeService;
import com.ofdun.jobfinder.features.resume.exception.ResumeNotFoundException;
import com.ofdun.jobfinder.features.skill.domain.model.SkillModel;
import com.ofdun.jobfinder.features.skill.domain.service.SkillService;
import jakarta.validation.Valid;
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
@RequestMapping("/api/v1/resumes")
@RequiredArgsConstructor
public class ResumeController {
    private final ResumeService resumeService;
    private final CategoryService categoryService;
    private final SkillService skillService;
    private final LanguageService languageService;
    private final ResumeApiMapper mapper;

    @GetMapping("/search")
    @PreAuthorize(
            "@sec.isEmployer(authentication) || (#applicantId != null &&"
                + " @sec.isApplicant(authentication) && @sec.isSelf(authentication, #applicantId))")
    public ResponseEntity<PageResponse<ResumeResponse>> searchResumes(
            @RequestParam(required = false) String q,
            @RequestParam(required = false) Long applicantId,
            @RequestParam(required = false) Long categoryId,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
                    Date creationDateFrom,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
                    Date creationDateTo,
            @RequestParam(required = false) List<Long> skillIds,
            @RequestParam(required = false) List<Long> languageIds,
            @Valid @ModelAttribute OffsetPaginationParams pagination) {

        var filter =
                new ResumeSearchFilter(
                        q,
                        applicantId,
                        categoryId,
                        creationDateFrom,
                        creationDateTo,
                        skillIds,
                        languageIds);

        var p =
                OffsetPagination.builder()
                        .limit(pagination.getLimit())
                        .offset(pagination.getOffset())
                        .sortBy(pagination.getSortBy())
                        .sortDesc(pagination.isSortDesc())
                        .build();

        var result = resumeService.searchResumes(filter, p);

        var items = result.getItems().stream().map(this::mapToResponse).toList();

        return ResponseEntity.ok(
                new PageResponse<>(
                        items,
                        result.getPage(),
                        result.getSize(),
                        result.getTotalElements(),
                        result.getTotalPages()));
    }

    @PostMapping
    @PreAuthorize(
            "@sec.isApplicant(authentication) && @sec.isSelf(authentication, #request.applicantId)")
    public ResponseEntity<@NonNull Long> createResume(@Valid @RequestBody ResumeRequest request) {
        ResumeModel model = mapper.toModel(request);
        Long id = resumeService.createResume(model);
        return ResponseEntity.status(HttpStatus.CREATED).body(id);
    }

    @GetMapping("/{id}")
    @PreAuthorize(
            "@sec.applicantOwnsResume(authentication, #id) || @sec.isEmployer(authentication)")
    public ResponseEntity<@NonNull ResumeResponse> getResume(@PathVariable Long id) {
        ResumeModel resume =
                resumeService.getResumeById(id).orElseThrow(() -> new ResumeNotFoundException(id));
        return ResponseEntity.ok(mapToResponse(resume));
    }

    @PutMapping("/{id}")
    @PreAuthorize("@sec.applicantOwnsResume(authentication, #id)")
    public ResponseEntity<@NonNull ResumeResponse> updateResume(
            @PathVariable Long id, @Valid @RequestBody ResumeUpdateRequest request) {
        ResumeModel model = mapper.toModel(request);
        model.setId(id);
        ResumeModel updated =
                resumeService
                        .updateResume(model)
                        .orElseThrow(() -> new ResumeNotFoundException(id));
        return ResponseEntity.ok(mapToResponse(updated));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("@sec.applicantOwnsResume(authentication, #id)")
    public ResponseEntity<@NonNull Void> deleteResume(@PathVariable Long id) {
        resumeService.deleteResume(id);
        return ResponseEntity.noContent().build();
    }

    private ResumeResponse mapToResponse(ResumeModel resume) {
        var category = categoryService.getCategoryById(resume.getCategoryId());
        List<SkillModel> skills =
                resume.getSkillIds() == null
                        ? null
                        : resume.getSkillIds().stream().map(skillService::getSkillById).toList();
        List<LanguageModel> languages =
                resume.getLanguageIds() == null
                        ? null
                        : resume.getLanguageIds().stream()
                                .map(languageService::getLanguageById)
                                .toList();
        return mapper.toResponse(resume, category, skills, languages);
    }
}
