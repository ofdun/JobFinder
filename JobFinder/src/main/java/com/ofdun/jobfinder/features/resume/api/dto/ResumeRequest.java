package com.ofdun.jobfinder.features.resume.api.dto;

import com.ofdun.jobfinder.features.education.api.dto.EducationCreateDto;
import com.ofdun.jobfinder.features.experience.api.dto.JobExperienceCreateDto;
import jakarta.validation.constraints.NotNull;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@AllArgsConstructor
@Data
public class ResumeRequest {
    @NotNull private Long applicantId;

    @NotNull private Long categoryId;

    private String description;
    private List<Long> skillIds;
    private List<EducationCreateDto> educations;
    private List<JobExperienceCreateDto> jobExperiences;
    private List<Long> languageIds;
}
