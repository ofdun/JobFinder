package com.ofdun.jobfinder.shared.education.model;

import com.ofdun.jobfinder.shared.resume.enums.EducationDegree;
import jakarta.validation.Valid;
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

    @NotNull @Valid private Long resumeId;

    @NotNull private EducationDegree educationDegree;

    @NotBlank private String institutionName;

    @NotBlank private String faculty;

    @NotBlank private String department; // кафедра

    @NotNull private Year yearOfGraduation;
}
