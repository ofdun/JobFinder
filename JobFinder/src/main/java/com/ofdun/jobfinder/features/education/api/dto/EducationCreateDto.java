package com.ofdun.jobfinder.features.education.api.dto;

import com.ofdun.jobfinder.features.resume.enums.EducationDegree;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class EducationCreateDto {
    @jakarta.validation.constraints.NotNull private EducationDegree educationDegree;
    @jakarta.validation.constraints.NotBlank private String institutionName;
    @jakarta.validation.constraints.NotBlank private String faculty;
    @jakarta.validation.constraints.NotBlank private String department;

    @jakarta.validation.constraints.NotNull
    @jakarta.validation.constraints.Min(1900)
    private Integer yearOfGraduation;

    @jakarta.validation.constraints.AssertTrue
    public boolean isGraduationYearValid() {
        return yearOfGraduation == null || yearOfGraduation <= java.time.Year.now().getValue() + 10;
    }
}
