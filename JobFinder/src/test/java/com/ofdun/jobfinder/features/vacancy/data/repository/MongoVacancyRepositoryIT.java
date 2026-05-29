package com.ofdun.jobfinder.features.vacancy.data.repository;

import static org.junit.jupiter.api.Assertions.*;

import com.ofdun.jobfinder.common.domain.model.OffsetPagination;
import com.ofdun.jobfinder.common.test.MongoRepositoryITBase;
import com.ofdun.jobfinder.features.vacancy.data.mongo.repository.MongoVacancyCRUDRepository;
import com.ofdun.jobfinder.features.vacancy.data.mongo.repository.MongoVacancyRepository;
import com.ofdun.jobfinder.features.vacancy.domain.model.VacancySearchFilter;
import com.ofdun.jobfinder.features.vacancy.enums.EmploymentType;
import com.ofdun.jobfinder.features.vacancy.enums.JobFormat;
import com.ofdun.jobfinder.features.vacancy.enums.PaymentFrequency;
import com.ofdun.jobfinder.features.vacancy.enums.VacancyStatus;
import java.math.BigDecimal;
import java.util.Date;
import java.util.List;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;

@Tag("mongo")
@ActiveProfiles("test-mongo")
@Import(MongoVacancyRepository.class)
class MongoVacancyRepositoryIT extends MongoRepositoryITBase {

    @Autowired private MongoVacancyRepository mongoVacancyRepository;
    @Autowired private MongoVacancyCRUDRepository mongoVacancyCRUDRepository;

    @Test
    void searchVacancies_filtersAndSorts() {
        mongoVacancyCRUDRepository.save(
                new com.ofdun.jobfinder.features.vacancy.data.mongo.entity.VacancyDocument(
                        1L,
                        10L,
                        1L,
                        BigDecimal.valueOf(100000),
                        List.of(1L),
                        List.of(1L),
                        PaymentFrequency.MONTHLY,
                        "2 years",
                        JobFormat.REMOTE,
                        EmploymentType.FULL_TIME,
                        "Java",
                        new Date(1000L),
                        "addr",
                        VacancyStatus.ACTIVE));
        mongoVacancyCRUDRepository.save(
                new com.ofdun.jobfinder.features.vacancy.data.mongo.entity.VacancyDocument(
                        2L,
                        10L,
                        2L,
                        BigDecimal.valueOf(150000),
                        List.of(2L),
                        List.of(1L),
                        PaymentFrequency.MONTHLY,
                        "3 years",
                        JobFormat.OFFICE,
                        EmploymentType.FULL_TIME,
                        "Go",
                        new Date(2000L),
                        "addr2",
                        VacancyStatus.INACTIVE));

        var result =
                mongoVacancyRepository.searchVacancies(
                        new VacancySearchFilter(
                                "java",
                                10L,
                                null,
                                BigDecimal.valueOf(90000),
                                BigDecimal.valueOf(120000),
                                PaymentFrequency.MONTHLY,
                                EmploymentType.FULL_TIME,
                                JobFormat.REMOTE,
                                null,
                                null,
                                List.of(1L),
                                null,
                                VacancyStatus.ACTIVE),
                        OffsetPagination.builder().limit(10).offset(0).sortBy("salary").build());

        assertEquals(1, result.getItems().size());
        assertEquals(1L, result.getItems().get(0).getId());
    }
}
