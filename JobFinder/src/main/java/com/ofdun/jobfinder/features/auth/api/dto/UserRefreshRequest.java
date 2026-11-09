package com.ofdun.jobfinder.features.auth.api.dto;

import com.ofdun.jobfinder.features.auth.enums.AccountType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record UserRefreshRequest(@NotBlank String refreshToken, @NotNull AccountType userType) {}
