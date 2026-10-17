package com.ofdun.jobfinder.cli.dto.vacancies;

import java.math.BigDecimal;
import java.util.List;

public record VacancyRequest(
        Long employerId,
        Long locationId,
        BigDecimal salary,
        List<Long> skillIds,
        List<Long> languageIds,
        PaymentFrequency paymentFrequency,
        String experience,
        JobFormat jobFormat,
        EmploymentType employmentType,
        String description,
        String address,
        VacancyStatus status) {}
