package com.ofdun.jobfinder.cli.dto.resumes;

import java.util.List;

public record ResumeRequest(
        Long applicantId,
        Long categoryId,
        String description,
        List<Long> skillIds,
        List<EducationCreate> educations,
        List<JobExperienceCreate> jobExperiences,
        List<Long> languageIds) {}
