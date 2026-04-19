package com.ofdun.jobfinder.cli.dto.resumes;

import java.time.LocalDate;

public record JobExperienceCreate(
        String position,
        String companyName,
        String description,
        LocalDate startDate,
        LocalDate endDate) {}
