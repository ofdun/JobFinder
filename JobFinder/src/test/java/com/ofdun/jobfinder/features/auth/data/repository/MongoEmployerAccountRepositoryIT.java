package com.ofdun.jobfinder.features.auth.data.repository;

import static org.junit.jupiter.api.Assertions.*;

import com.ofdun.jobfinder.common.test.MongoRepositoryITBase;
import com.ofdun.jobfinder.features.auth.domain.repository.EmployerAccountRepository;
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
@Import({MongoEmployerAccountRepository.class, MongoEmployerRepository.class})
class MongoEmployerAccountRepositoryIT extends MongoRepositoryITBase {

    @Autowired private EmployerAccountRepository employerAccountRepository;
    @Autowired private EmployerRepository employerRepository;

    @Test
    void findByEmail_whenExists_thenReturnsAccount() {
        var employer =
                new EmployerModel(
                        1L,
                        "Company",
                        "hash",
                        "desc",
                        "address",
                        "https://site.example",
                        "company.mongo@example.com",
                        1L);
        employerRepository.createEmployer(employer);

        var account = employerAccountRepository.findByEmail("company.mongo@example.com");

        assertNotNull(account);
        assertEquals(1L, account.getId());
        assertEquals("hash", account.getPasswordHash());
    }

    @Test
    void findByEmail_whenMissing_thenReturnsNull() {
        var account = employerAccountRepository.findByEmail("missing.mongo@example.com");

        assertNull(account);
    }
}
