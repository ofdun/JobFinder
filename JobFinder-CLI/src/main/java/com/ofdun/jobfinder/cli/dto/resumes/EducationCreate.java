package com.ofdun.jobfinder.cli.dto.resumes;

public record EducationCreate(
        EducationDegree educationDegree,
        String institutionName,
        String faculty,
        String department,
        Integer yearOfGraduation) {}
