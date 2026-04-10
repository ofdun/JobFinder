package com.ofdun.jobfinder.features.education.domain.model;

import com.ofdun.jobfinder.features.resume.enums.EducationDegree;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.time.Year;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.ToString;

@AllArgsConstructor
@Data
@ToString
public class EducationModel {
    private Long id;

    private Long resumeId;

    @NotNull private EducationDegree educationDegree;

    @NotBlank private String institutionName;

    @NotBlank private String faculty;

    @NotBlank private String department; // кафедра

    @NotNull private Year yearOfGraduation;
}
