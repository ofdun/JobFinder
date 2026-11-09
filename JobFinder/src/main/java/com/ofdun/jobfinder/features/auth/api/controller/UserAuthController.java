package com.ofdun.jobfinder.features.auth.api.controller;

import com.ofdun.jobfinder.features.api.ApiFailure;
import com.ofdun.jobfinder.features.applicant.exception.ApplicantNotFoundException;
import com.ofdun.jobfinder.features.auth.api.dto.TokenPairResponse;
import com.ofdun.jobfinder.features.auth.api.dto.UserLoginRequest;
import com.ofdun.jobfinder.features.auth.api.dto.UserRefreshRequest;
import com.ofdun.jobfinder.features.auth.domain.service.ApplicantAuthService;
import com.ofdun.jobfinder.features.auth.domain.service.AuthService;
import com.ofdun.jobfinder.features.auth.domain.service.EmployerAuthService;
import com.ofdun.jobfinder.features.auth.enums.AccountType;
import com.ofdun.jobfinder.features.employer.exception.EmployerNotFoundException;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v2/auth")
@RequiredArgsConstructor
public class UserAuthController {
    private final ApplicantAuthService applicantAuthService;
    private final EmployerAuthService employerAuthService;

    @PostMapping("/login")
    public TokenPairResponse login(@Valid @RequestBody UserLoginRequest request) {
        try {
            var pair = service(request.userType()).login(request.email(), request.password());
            return new TokenPairResponse(pair.accessToken(), pair.refreshToken());
        } catch (ApplicantNotFoundException | EmployerNotFoundException ex) {
            throw new ApiFailure("UNAUTHENTICATED", 401, "Invalid credentials");
        }
    }

    @PostMapping("/refresh")
    public TokenPairResponse refresh(@Valid @RequestBody UserRefreshRequest request) {
        var pair = service(request.userType()).refreshToken(request.refreshToken());
        return new TokenPairResponse(pair.accessToken(), pair.refreshToken());
    }

    private AuthService service(AccountType type) {
        return switch (type) {
            case APPLICANT -> applicantAuthService;
            case EMPLOYER -> employerAuthService;
        };
    }
}
