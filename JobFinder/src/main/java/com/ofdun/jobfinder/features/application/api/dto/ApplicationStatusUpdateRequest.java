package com.ofdun.jobfinder.features.application.api.dto;

import com.ofdun.jobfinder.features.application.enums.ApplicationStatus;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class ApplicationStatusUpdateRequest {
    @NotNull private ApplicationStatus applicationStatus;
}

