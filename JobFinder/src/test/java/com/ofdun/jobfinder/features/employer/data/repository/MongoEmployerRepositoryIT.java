package com.ofdun.jobfinder.features.employer.data.repository;

import static org.junit.jupiter.api.Assertions.*;

import com.ofdun.jobfinder.common.test.MongoRepositoryITBase;
import com.ofdun.jobfinder.features.employer.data.mongo.repository.MongoEmployerRepository;
import com.ofdun.jobfinder.features.employer.domain.model.EmployerModel;
import com.ofdun.jobfinder.features.employer.domain.repository.EmployerRepository;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;

@Tag("mongo")
@ActiveProfiles("test-mongo")
@Import(MongoEmployerRepository.class)
class MongoEmployerRepositoryIT extends MongoRepositoryITBase {

    @Autowired private EmployerRepository employerRepository;

    @Test
    void getEmployerByEmail_whenExists_thenReturnsEmployer() {
        var employer =
                new EmployerModel(
                        2L,
                        "Company",
                        "hash",
                        "desc",
                        "address",
                        "https://site.example",
                        "company.mongo@example.com",
                        5L);
        employerRepository.createEmployer(employer);

        var found = employerRepository.getEmployerByEmail("company.mongo@example.com");

        assertTrue(found.isPresent());
        assertEquals("Company", found.get().getName());
    }
}
