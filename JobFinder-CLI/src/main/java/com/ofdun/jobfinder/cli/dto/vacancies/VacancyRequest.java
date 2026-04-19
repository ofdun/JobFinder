package com.ofdun.jobfinder.cli.dto.vacancies;

import java.util.List;

public record VacancyRequest(
        Long employerId,
        Long locationId,
        Double salary,
        List<Long> skillIds,
        List<Long> languageIds,
        PaymentFrequency paymentFrequency,
        String experience,
        JobFormat jobFormat,
        EmploymentType employmentType,
        String description,
        String address) {}
