package com.ofdun.jobfinder.cli.dto.resumes;

import java.time.LocalDate;

public record Education(
        Long id,
        Long resumeId,
        EducationDegree educationDegree,
        String institutionName,
        String faculty,
        String department,
        LocalDate yearOfGraduation) {}
