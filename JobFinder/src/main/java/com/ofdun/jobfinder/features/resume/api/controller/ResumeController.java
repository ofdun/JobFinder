package com.ofdun.jobfinder.features.resume.api.controller;

import com.ofdun.jobfinder.features.category.domain.service.CategoryService;
import com.ofdun.jobfinder.features.language.domain.model.LanguageModel;
import com.ofdun.jobfinder.features.language.domain.service.LanguageService;
import com.ofdun.jobfinder.features.resume.api.dto.ResumeUpdateRequest;
import com.ofdun.jobfinder.features.resume.api.dto.ResumeRequest;
import com.ofdun.jobfinder.features.resume.api.dto.ResumeResponse;
import com.ofdun.jobfinder.features.resume.api.mapper.ResumeApiMapper;
import com.ofdun.jobfinder.features.resume.domain.model.ResumeModel;
import com.ofdun.jobfinder.features.resume.domain.service.ResumeService;
import com.ofdun.jobfinder.features.skill.domain.model.SkillModel;
import com.ofdun.jobfinder.features.skill.domain.service.SkillService;
import com.ofdun.jobfinder.features.resume.exception.ResumeNotFoundException;
import jakarta.validation.Valid;
import java.util.List;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
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

    @PostMapping
    @PreAuthorize("@sec.isApplicant(authentication) && @sec.isSelf(authentication, #request.applicantId)")
    public ResponseEntity<@NonNull Long> createResume(@Valid @RequestBody ResumeRequest request) {
        ResumeModel model = mapper.toModel(request);
        Long id = resumeService.createResume(model);
        return ResponseEntity.status(HttpStatus.CREATED).body(id);
    }

    @GetMapping("/{id}")
    @PreAuthorize("@sec.applicantOwnsResume(authentication, #id) || @sec.isEmployer(authentication)")
    public ResponseEntity<@NonNull ResumeResponse> getResume(@PathVariable Long id) {
        ResumeModel resume = resumeService.getResumeById(id)
                .orElseThrow(() -> new ResumeNotFoundException(id));
        return ResponseEntity.ok(mapToResponse(resume));
    }

    @PutMapping("/{id}")
    @PreAuthorize("@sec.applicantOwnsResume(authentication, #id)")
    public ResponseEntity<@NonNull ResumeResponse> updateResume(
            @PathVariable Long id,
            @Valid @RequestBody ResumeUpdateRequest request) {
        ResumeModel model = mapper.toModel(request);
        model.setId(id);
        ResumeModel updated = resumeService.updateResume(model)
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
        List<SkillModel> skills = resume.getSkillIds() == null
                ? null
                : resume.getSkillIds().stream().map(skillService::getSkillById).toList();
        List<LanguageModel> languages = resume.getLanguageIds() == null
                ? null
                : resume.getLanguageIds().stream().map(languageService::getLanguageById).toList();
        return mapper.toResponse(resume, category, skills, languages);
    }
}
