package com.ofdun.jobfinder.features.applicant.data.repository;

import static org.junit.jupiter.api.Assertions.*;

import com.ofdun.jobfinder.common.test.MongoRepositoryITBase;
import com.ofdun.jobfinder.features.applicant.data.mongo.repository.MongoApplicantRepository;
import com.ofdun.jobfinder.features.applicant.domain.model.ApplicantModel;
import com.ofdun.jobfinder.features.applicant.domain.repository.ApplicantRepository;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;

@Tag("mongo")
@ActiveProfiles("test-mongo")
@Import(MongoApplicantRepository.class)
class MongoApplicantRepositoryIT extends MongoRepositoryITBase {

    @Autowired private ApplicantRepository applicantRepository;

    @Test
    void createApplicant_whenValid_thenReturnsId() {
        var applicant =
                new ApplicantModel(
                        1L,
                        "Alice",
                        "alice.mongo@example.com",
                        "pass",
                        "address",
                        "+79990000000",
                        10L);

        var id = applicantRepository.createApplicant(applicant);

        assertEquals(1L, id);
    }

    @Test
    void getApplicantByEmail_whenExists_thenReturnsApplicant() {
        var applicant =
                new ApplicantModel(
                        1L,
                        "Alice",
                        "alice.mongo@example.com",
                        "pass",
                        "address",
                        "+79990000000",
                        10L);
        applicantRepository.createApplicant(applicant);

        var found = applicantRepository.getApplicantByEmail("alice.mongo@example.com");

        assertTrue(found.isPresent());
        assertEquals("Alice", found.get().getName());
    }

    @Test
    void deleteApplicant_whenExists_thenDeleted() {
        var applicant = new ApplicantModel(7L, "Bob", "bob.mongo@example.com", "pass", "a", "+7000", 1L);
        applicantRepository.createApplicant(applicant);

        var deleted = applicantRepository.deleteApplicant(7L);
        var found = applicantRepository.getApplicantById(7L);

        assertTrue(deleted);
        assertTrue(found.isEmpty());
    }
}
