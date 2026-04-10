package com.ofdun.jobfinder.features.application.domain.model;

import com.ofdun.jobfinder.features.application.enums.ApplicationStatus;
import jakarta.validation.constraints.NotNull;
import java.util.Date;
import lombok.AllArgsConstructor;
import lombok.Data;

@AllArgsConstructor
@Data
public class ApplicationModel {
    private Long id;

    @NotNull private Long vacancyId;

    @NotNull private Long resumeId;

    @NotNull private Date applicationDate;

    @NotNull private ApplicationStatus applicationStatus;
}
