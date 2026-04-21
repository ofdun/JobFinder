package com.ofdun.jobfinder.features.application.api.controller;

import com.ofdun.jobfinder.features.application.api.dto.ApplicationResponse;
import com.ofdun.jobfinder.features.application.api.dto.ApplicationStatusUpdateRequest;
import com.ofdun.jobfinder.features.application.api.mapper.ApplicationApiMapper;
import com.ofdun.jobfinder.features.application.domain.service.ApplicationService;
import jakarta.validation.Valid;
import java.util.List;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1")
@RequiredArgsConstructor
public class EmployerVacancyApplicationController {
    private final ApplicationService applicationService;
    private final ApplicationApiMapper mapper;

    @GetMapping("/vacancies/{vacancyId}/applications")
    @PreAuthorize("@sec.employerOwnsVacancy(authentication, #vacancyId)")
    public ResponseEntity<@NonNull List<ApplicationResponse>> getApplicationsByVacancy(
            @PathVariable Long vacancyId) {
        var applications = applicationService.getApplicationsByVacancyId(vacancyId);
        return ResponseEntity.ok(applications.stream().map(mapper::toResponse).toList());
    }

    @PatchMapping("/applications/{id}/status")
    @PreAuthorize("@sec.isEmployer(authentication) && @sec.canAccessApplication(authentication, #id)")
    public ResponseEntity<@NonNull ApplicationResponse> updateApplicationStatus(
            @PathVariable Long id, @Valid @RequestBody ApplicationStatusUpdateRequest request) {
        var existing = applicationService.getApplication(id);
        existing.setApplicationStatus(request.getApplicationStatus());
        var updated = applicationService.updateApplication(existing);
        return ResponseEntity.ok(mapper.toResponse(updated));
    }
}

