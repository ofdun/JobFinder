package com.ofdun.jobfinder.features.auth.data.repository;

import static org.junit.jupiter.api.Assertions.*;

import com.ofdun.jobfinder.common.test.MongoRepositoryITBase;
import com.ofdun.jobfinder.features.applicant.data.mongo.repository.MongoApplicantRepository;
import com.ofdun.jobfinder.features.applicant.domain.model.ApplicantModel;
import com.ofdun.jobfinder.features.applicant.domain.repository.ApplicantRepository;
import com.ofdun.jobfinder.features.auth.domain.repository.ApplicantAccountRepository;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;

@Tag("mongo")
@ActiveProfiles("test-mongo")
@Import({MongoApplicantAccountRepository.class, MongoApplicantRepository.class})
class MongoApplicantAccountRepositoryIT extends MongoRepositoryITBase {

    @Autowired private ApplicantAccountRepository applicantAccountRepository;
    @Autowired private ApplicantRepository applicantRepository;

    @Test
    void findByEmail_whenExists_thenReturnsAccount() {
        var applicant =
                new ApplicantModel(
                        1L,
                        "Alice",
                        "alice.mongo@example.com",
                        "hash",
                        "address",
                        "+79990000000",
                        1L);

        applicantRepository.createApplicant(applicant);

        var account = applicantAccountRepository.findByEmail("alice.mongo@example.com");

        assertTrue(account.isPresent());
        assertEquals(1L, account.get().getId());
        assertEquals("hash", account.get().getPasswordHash());
    }

    @Test
    void findByEmail_whenMissing_thenReturnsEmpty() {
        var account = applicantAccountRepository.findByEmail("missing.mongo@example.com");

        assertTrue(account.isEmpty());
    }
}
