package com.ofdun.jobfinder.features.resume.domain.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.ToString;

import java.time.Year;

@AllArgsConstructor
@Data
@ToString
public class EducationModel {
    private Long resumeId;
    private EducationDegree educationDegree;
    private String institutionName;
    private String college; // факультет
    private String department; // кафедра
    private Year yearOfGraduation;
}
