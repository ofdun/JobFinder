package com.ofdun.jobfinder.cli.dto.vacancies;

import com.ofdun.jobfinder.cli.dto.common.Language;
import com.ofdun.jobfinder.cli.dto.common.Location;
import com.ofdun.jobfinder.cli.dto.common.Skill;
import java.time.OffsetDateTime;
import java.util.List;

public record Vacancy(
        Long id,
        Long employerId,
        Location location,
        Double salary,
        List<Skill> skills,
        List<Language> languages,
        PaymentFrequency paymentFrequency,
        String experience,
        JobFormat jobFormat,
        EmploymentType employmentType,
        String description,
        OffsetDateTime publicationDate,
        String address) {}
