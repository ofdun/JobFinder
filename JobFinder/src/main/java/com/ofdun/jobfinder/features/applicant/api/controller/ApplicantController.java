package com.ofdun.jobfinder.features.applicant.api.controller;

import com.ofdun.jobfinder.features.applicant.api.dto.ApplicantRequest;
import com.ofdun.jobfinder.features.applicant.api.dto.ApplicantResponse;
import com.ofdun.jobfinder.features.applicant.api.mapper.ApplicantApiMapper;
import com.ofdun.jobfinder.features.applicant.domain.model.ApplicantModel;
import com.ofdun.jobfinder.features.applicant.domain.service.ApplicantService;
import com.ofdun.jobfinder.features.location.domain.service.LocationService;
import jakarta.validation.Valid;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/applicants")
@RequiredArgsConstructor
public class ApplicantController {
    private final ApplicantService applicantService;
    private final LocationService locationService;
    private final ApplicantApiMapper mapper;

    @PostMapping
    public ResponseEntity<@NonNull Long> createApplicant(@Valid @RequestBody ApplicantRequest request) {
        return ResponseEntity.ok(applicantService.createApplicant(mapper.toModel(request)));
    }

    @GetMapping("/{id}")
    @PreAuthorize("@sec.isEmployer(authentication) || (@sec.isApplicant(authentication) && @sec.isSelf(authentication, #id))")
    public ResponseEntity<@NonNull ApplicantResponse> getApplicant(@PathVariable Long id) {
        var applicant = applicantService.getApplicantById(id);
        var location = locationService.getLocationById(applicant.getLocationId());
        return ResponseEntity.ok(mapper.toResponse(applicant, location));
    }

    @PutMapping("/{id}")
    @PreAuthorize("@sec.isApplicant(authentication) && @sec.isSelf(authentication, #id)")
    public ResponseEntity<@NonNull ApplicantResponse> updateApplicant(
            @PathVariable Long id, @Valid @RequestBody ApplicantRequest request) {
        ApplicantModel model = mapper.toModel(request);
        model.setId(id);
        var location = locationService.getLocationById(model.getLocationId());
        var dto = applicantService.updateApplicant(model);
        return ResponseEntity.ok(mapper.toResponse(dto, location));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("@sec.isApplicant(authentication) && @sec.isSelf(authentication, #id)")
    public ResponseEntity<@NonNull Void> deleteApplicant(@PathVariable Long id) {
        applicantService.deleteApplicant(id);
        return ResponseEntity.noContent().build();
    }
}
