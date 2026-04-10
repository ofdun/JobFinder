package com.ofdun.jobfinder.features.employer.api.controller;

import com.ofdun.jobfinder.features.employer.api.dto.EmployerRequest;
import com.ofdun.jobfinder.features.employer.api.dto.EmployerResponse;
import com.ofdun.jobfinder.features.employer.api.mapper.EmployerApiMapper;
import com.ofdun.jobfinder.features.employer.domain.model.EmployerModel;
import com.ofdun.jobfinder.features.employer.domain.service.EmployerService;
import com.ofdun.jobfinder.features.location.domain.service.LocationService;
import jakarta.validation.Valid;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/employers")
@RequiredArgsConstructor
public class EmployerController {
    private final EmployerService employerService;
    private final LocationService locationService;
    private final EmployerApiMapper mapper;

    @PostMapping
    public ResponseEntity<@NonNull Long> createEmployer(@Valid @RequestBody EmployerRequest request) {
        return ResponseEntity.ok(employerService.createEmployer(mapper.toModel(request)));
    }

    @GetMapping("/{id}")
    @PreAuthorize("@sec.isApplicant(authentication) || (@sec.isEmployer(authentication) && @sec.isSelf(authentication, #id))")
    public ResponseEntity<@NonNull EmployerResponse> getEmployer(@PathVariable Long id) {
        var employer = employerService.getEmployerById(id);
        var location = locationService.getLocationById(employer.getLocationId());
        return ResponseEntity.ok(mapper.toResponse(employer, location));
    }

    @PutMapping("/{id}")
    @PreAuthorize("@sec.isEmployer(authentication) && @sec.isSelf(authentication, #id)")
    public ResponseEntity<@NonNull EmployerResponse> updateEmployer(
            @PathVariable Long id, @Valid @RequestBody EmployerRequest request) {
        EmployerModel model = mapper.toModel(request);
        model.setId(id);
        var location = locationService.getLocationById(model.getLocationId());
        var employer = employerService.updateEmployer(model);
        return ResponseEntity.ok(mapper.toResponse(employer, location));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("@sec.isEmployer(authentication) && @sec.isSelf(authentication, #id)")
    public ResponseEntity<@NonNull Void> deleteEmployer(@PathVariable Long id) {
        employerService.deleteEmployer(id);
        return ResponseEntity.noContent().build();
    }
}
