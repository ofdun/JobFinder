package com.ofdun.jobfinder.cli.dto.vacancies;

import java.util.List;

public record VacancyPageResponse(
        List<Vacancy> items, Integer page, Integer size, Long totalElements, Integer totalPages) {}
