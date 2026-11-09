package com.ofdun.jobfinder.cli.dto.resumes;

public record Education(
        Long id,
        Long resumeId,
        EducationDegree educationDegree,
        String institutionName,
        String faculty,
        String department,
        Integer yearOfGraduation) {}
