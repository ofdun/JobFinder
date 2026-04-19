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
@RequestMapping("/api/v1/applications")
@RequiredArgsConstructor
public class ApplicationController {
    private final ApplicationService applicationService;
    private final ApplicationApiMapper mapper;

    @PostMapping
    @PreAuthorize("@sec.isApplicant(authentication)")
    public ResponseEntity<@NonNull Long> createApplication(
            @Valid @RequestBody ApplicationRequest request) {
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
        ApplicationModel model = mapper.toModel(request);
        model.setId(id);
        var dto = applicationService.updateApplication(model);
        return ResponseEntity.ok(mapper.toResponse(dto));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("@sec.canAccessApplication(authentication, #id)")
    public ResponseEntity<@NonNull Void> deleteApplication(@PathVariable Long id) {
        applicationService.deleteApplication(id);
        return ResponseEntity.noContent().build();
    }
}
