package com.ofdun.jobfinder.features.resume.api.dto;

import com.ofdun.jobfinder.features.category.api.dto.CategoryDto;
import com.ofdun.jobfinder.features.education.api.dto.EducationDto;
import com.ofdun.jobfinder.features.experience.api.dto.JobExperienceDto;
import com.ofdun.jobfinder.features.language.api.dto.LanguageDto;
import com.ofdun.jobfinder.features.skill.api.dto.SkillDto;
import java.util.Date;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ResumeResponse {
    private Long id;
    private Long applicantId;
    private CategoryDto category;
    private String description;
    private List<SkillDto> skills;
    private List<EducationDto> educations;
    private List<JobExperienceDto> jobExperiences;
    private List<LanguageDto> languages;
    private Date date;
}
