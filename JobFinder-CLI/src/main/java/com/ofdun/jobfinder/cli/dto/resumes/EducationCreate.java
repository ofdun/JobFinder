package com.ofdun.jobfinder.cli.dto.resumes;

import java.util.Date;

public record EducationCreate(
        EducationDegree educationDegree,
        String institutionName,
        String faculty,
        String department,
        Date yearOfGraduation) {}
