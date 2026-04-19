package com.ofdun.jobfinder.cli.dto.resumes;

import com.ofdun.jobfinder.cli.dto.common.Category;
import com.ofdun.jobfinder.cli.dto.common.Language;
import com.ofdun.jobfinder.cli.dto.common.Skill;
import java.time.OffsetDateTime;
import java.util.List;

public record Resume(
        Long id,
        Long applicantId,
        Category category,
        String description,
        List<Skill> skills,
        List<Education> educations,
        List<JobExperience> jobExperiences,
        List<Language> languages,
        OffsetDateTime date) {}
