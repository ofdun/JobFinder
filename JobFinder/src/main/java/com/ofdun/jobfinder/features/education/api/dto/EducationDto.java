package com.ofdun.jobfinder.features.education.api.dto;

import com.ofdun.jobfinder.features.resume.enums.EducationDegree;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class EducationDto {
    private Long id;
    private Long resumeId;
    private EducationDegree educationDegree;
    private String institutionName;
    private String faculty;
    private String department;
    private Integer yearOfGraduation;
}
