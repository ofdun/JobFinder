package com.ofdun.jobfinder.features.resume.domain.model;

import com.ofdun.jobfinder.shared.resume.domain.enums.EducationDegree;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.ToString;

import java.time.Year;

@AllArgsConstructor
@Data
@ToString
public class EducationModel {
    @NotNull
    private Long resumeId;

    @NotNull
    private EducationDegree educationDegree;

    @NotBlank
    private String institutionName;

    @NotBlank
    private String faculty;

    @NotBlank
    private String department; // кафедра

    @NotNull
    private Year yearOfGraduation;
}
