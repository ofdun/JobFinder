package com.ofdun.jobfinder.features.resume.domain.model;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.ToString;

import java.util.Date;

@AllArgsConstructor
@Data
@ToString
public class JobExperience {
    @NotNull
    private Long resumeId;

    @NotBlank
    private String position;

    @NotBlank
    private String companyName;

    @NotBlank
    private String description;

    @NotNull
    private Date startDate;

    @NotNull
    private Date endDate;
}
