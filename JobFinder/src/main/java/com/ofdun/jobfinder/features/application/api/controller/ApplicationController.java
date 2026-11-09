package com.ofdun.jobfinder.features.application.api.controller;

import com.ofdun.jobfinder.features.application.api.dto.ApplicationRequest;
import com.ofdun.jobfinder.features.application.api.dto.ApplicationResponse;
import com.ofdun.jobfinder.features.application.api.mapper.ApplicationApiMapper;
import com.ofdun.jobfinder.features.application.domain.model.ApplicationModel;
import com.ofdun.jobfinder.features.application.domain.service.ApplicationService;
import jakarta.validation.Valid;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping({"/api/v1/applications", "/api/v2/applications"})
@RequiredArgsConstructor
public class ApplicationController {
    @org.springframework.beans.factory.annotation.Autowired
    private com.ofdun.jobfinder.features.api.ApiAccess apiAccess;

    @org.springframework.beans.factory.annotation.Autowired
    private jakarta.servlet.http.HttpServletRequest servletRequest;

    private boolean v2() {
        return servletRequest != null && servletRequest.getRequestURI().startsWith("/api/v2/");
    }

    private org.springframework.security.core.Authentication actor() {
        return org.springframework.security.core.context.SecurityContextHolder.getContext()
                .getAuthentication();
    }

    private final ApplicationService applicationService;
    private final ApplicationApiMapper mapper;

    @GetMapping
    @PreAuthorize("@sec.isApplicant(authentication)")
    public java.util.List<ApplicationResponse> myApplications(
            @RequestParam(defaultValue = "20") int limit,
            @RequestParam(defaultValue = "0") int offset) {
        if (limit < 1 || limit > 100 || offset < 0)
            throw new IllegalArgumentException("Invalid pagination");
        var applicant =
                apiAccess.role(
                        actor(), com.ofdun.jobfinder.features.auth.enums.AccountType.APPLICANT);
        return applicationService
                .getApplicationsByApplicantId(applicant.id(), limit, offset)
                .stream()
                .map(mapper::toResponse)
                .toList();
    }

    @PostMapping
    @PreAuthorize("@sec.isApplicant(authentication)")
    public ResponseEntity<@NonNull Long> createApplication(
            @Valid @RequestBody ApplicationRequest request) {
        if (v2()) apiAccess.newApplication(actor(), request.getVacancyId(), request.getResumeId());
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(applicationService.saveApplication(mapper.toModel(request)));
    }

    @GetMapping("/{id}")
    @PreAuthorize("@sec.canAccessApplication(authentication, #id)")
    public ResponseEntity<@NonNull ApplicationResponse> getApplication(@PathVariable Long id) {
        var application = applicationService.getApplication(id);
        return ResponseEntity.ok(mapper.toResponse(application));
    }

    @PutMapping("/{id}")
    @PreAuthorize("@sec.canAccessApplication(authentication, #id)")
    public ResponseEntity<@NonNull ApplicationResponse> updateApplication(
            @PathVariable Long id, @Valid @RequestBody ApplicationRequest request) {
        var existing = v2() ? apiAccess.employerApplication(actor(), id) : null;
        if (existing != null
                && (!existing.getVacancyId().equals(request.getVacancyId())
                        || !existing.getResumeId().equals(request.getResumeId())))
            throw new com.ofdun.jobfinder.features.api.ApiFailure(
                    "CONFLICT", 409, "Application relationships cannot change");
        ApplicationModel model = mapper.toModel(request);
        if (existing != null) model.setApplicationDate(existing.getApplicationDate());
        model.setId(id);
        var dto = applicationService.updateApplication(model);
        return ResponseEntity.ok(mapper.toResponse(dto));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("@sec.canAccessApplication(authentication, #id)")
    public ResponseEntity<@NonNull Void> deleteApplication(@PathVariable Long id) {
        if (v2()) apiAccess.applicantApplication(actor(), id);
        applicationService.deleteApplication(id);
        return ResponseEntity.noContent().build();
    }
}
