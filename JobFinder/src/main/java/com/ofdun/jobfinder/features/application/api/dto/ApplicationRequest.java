package com.ofdun.jobfinder.features.application.api.dto;

import com.ofdun.jobfinder.features.application.enums.ApplicationStatus;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class ApplicationRequest {
    @NotNull
    private Long vacancyId;

    @NotNull
    private Long resumeId;

    @NotNull
    private ApplicationStatus applicationStatus;
}
