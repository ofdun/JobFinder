package com.ofdun.jobfinder.features.education.api.dto;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.ofdun.jobfinder.features.resume.enums.EducationDegree;
import java.time.Year;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class EducationCreateDto {
    private EducationDegree educationDegree;
    private String institutionName;
    private String faculty;
    private String department;

    @JsonDeserialize(using = YearFromStringDeserializer.class)
    private Year yearOfGraduation;
}
