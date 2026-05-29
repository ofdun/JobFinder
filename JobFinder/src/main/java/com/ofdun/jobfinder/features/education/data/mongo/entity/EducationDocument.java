package com.ofdun.jobfinder.features.education.data.mongo.entity;

import com.ofdun.jobfinder.features.resume.enums.EducationDegree;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@Data
@Document(collection = "educations")
@AllArgsConstructor
@NoArgsConstructor
public class EducationDocument {
    @Id
    private Long id;

    @NotNull private Long resumeId;

    @NotNull private EducationDegree degree;

    @NotBlank private String institution;

    @NotBlank private String faculty;

    @NotBlank private String department;

    @NotNull private Integer graduationYear;
}

