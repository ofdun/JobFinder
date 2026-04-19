package com.ofdun.jobfinder.cli.dto.resumes;

import java.time.LocalDate;

public record JobExperience(
        Long id,
        Long resumeId,
        String position,
        String companyName,
        String description,
        LocalDate startDate,
        LocalDate endDate) {}
