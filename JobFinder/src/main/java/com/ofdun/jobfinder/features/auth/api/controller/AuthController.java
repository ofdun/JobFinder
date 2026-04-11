package com.ofdun.jobfinder.features.auth.api.controller;

import com.ofdun.jobfinder.features.auth.api.dto.LoginRequest;
import com.ofdun.jobfinder.features.auth.api.dto.RefreshRequest;
import com.ofdun.jobfinder.features.auth.api.dto.TokenPairResponse;
import com.ofdun.jobfinder.features.auth.domain.service.ApplicantAuthService;
import com.ofdun.jobfinder.features.auth.domain.service.EmployerAuthService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
public class AuthController {
    private final ApplicantAuthService applicantAuthService;
    private final EmployerAuthService employerAuthService;

    @PostMapping("/applicant/login")
    public TokenPairResponse applicantLogin(@Valid @RequestBody LoginRequest request) {
        var pair = applicantAuthService.login(request.email(), request.password());
        return new TokenPairResponse(pair.accessToken(), pair.refreshToken());
    }

    @PostMapping("/employer/login")
    public TokenPairResponse employerLogin(@Valid @RequestBody LoginRequest request) {
        var pair = employerAuthService.login(request.email(), request.password());
        return new TokenPairResponse(pair.accessToken(), pair.refreshToken());
    }

    @PostMapping("/applicant/refresh")
    public TokenPairResponse applicantRefresh(@Valid @RequestBody RefreshRequest request) {
        var pair = applicantAuthService.refreshToken(request.refreshToken());
        return new TokenPairResponse(pair.accessToken(), pair.refreshToken());
    }

    @PostMapping("/employer/refresh")
    public TokenPairResponse employerRefresh(@Valid @RequestBody RefreshRequest request) {
        var pair = employerAuthService.refreshToken(request.refreshToken());
        return new TokenPairResponse(pair.accessToken(), pair.refreshToken());
    }
}
