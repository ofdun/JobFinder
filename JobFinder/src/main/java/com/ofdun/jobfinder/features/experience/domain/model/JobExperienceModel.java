package com.ofdun.jobfinder.features.experience.domain.model;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDate;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.ToString;

@AllArgsConstructor
@Data
@ToString
public class JobExperienceModel {
    private Long id;

    private Long resumeId;

    @NotBlank private String position;

    @NotBlank private String companyName;

    private String description;

    @NotNull private LocalDate startDate;

    @NotNull private LocalDate endDate;
}
