package com.ofdun.jobfinder.features.auth.api.dto;

import com.ofdun.jobfinder.features.auth.enums.AccountType;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record UserLoginRequest(
        @Email @NotBlank String email, @NotBlank String password, @NotNull AccountType userType) {}
