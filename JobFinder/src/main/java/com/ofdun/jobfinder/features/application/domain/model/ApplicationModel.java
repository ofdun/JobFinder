package com.ofdun.jobfinder.features.application.domain.model;

import com.ofdun.jobfinder.shared.application.domain.enums.ApplicationStatus;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.Date;

@AllArgsConstructor
@Data
public class ApplicationModel {
    private Long id;

    @NotNull
    private Long vacancyId;

    @NotNull
    private Long resumeId;

    @NotNull
    private Date applicationDate;

    @NotNull
    private ApplicationStatus applicationStatus;
}
