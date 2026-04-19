package com.ofdun.jobfinder.features.applicant.data.repository;

import static org.junit.jupiter.api.Assertions.*;

import com.ofdun.jobfinder.features.applicant.domain.model.ApplicantModel;
import com.ofdun.jobfinder.features.applicant.domain.repository.ApplicantRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.context.annotation.Import;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.utility.DockerImageName;

@ActiveProfiles("test")
@DataJpaTest
@Import(PostgreSQLApplicantRepository.class)
@Testcontainers
class PostgreSQLApplicantRepositoryIT {

    @Container
    static final GenericContainer<?> postgres =
            new GenericContainer<>(DockerImageName.parse("postgres:15-alpine"))
                    .withExposedPorts(5432)
                    .withEnv("POSTGRES_DB", "test")
                    .withEnv("POSTGRES_USER", "test")
                    .withEnv("POSTGRES_PASSWORD", "test");

    @DynamicPropertySource
    static void registerPgProperties(DynamicPropertyRegistry registry) {
        registry.add(
                "spring.datasource.url",
                () ->
                        String.format(
                                "jdbc:postgresql://%s:%d/test",
                                postgres.getHost(), postgres.getMappedPort(5432)));
        registry.add("spring.datasource.username", () -> "test");
        registry.add("spring.datasource.password", () -> "test");
    }

    @Autowired private ApplicantRepository applicantRepository;

    @BeforeEach
    void cleanup() {}

    @Test
    void createApplicant_whenValidApplicant_thenIdReturned() {
        ApplicantModel model = buildApplicantModel("newuser@example.com");

        Long id = applicantRepository.createApplicant(model);

        assertNotNull(id, "id должен быть не null после сохранения");

        var fromDbOpt = applicantRepository.getApplicantById(id);
        assertTrue(fromDbOpt.isPresent(), "найденный по id кандидат не должен быть null");
        assertEquals("newuser@example.com", fromDbOpt.get().getEmail());
    }

    @Test
    void getApplicantByEmail_whenExists_thenReturnApplicant() {
        String email = "bob@example.com";

        var fromDbOpt = applicantRepository.getApplicantByEmail(email);
        assertTrue(fromDbOpt.isPresent(), "предзагруженный кандидат с email должен существовать");
        assertEquals(email, fromDbOpt.get().getEmail());
    }

    @Test
    void updateApplicant_whenApplicantExists_thenFieldsUpdated() {
        long id = 2;
        var savedOpt = applicantRepository.getApplicantById(id);
        assertTrue(
                savedOpt.isPresent(),
                "предзагруженный кандидат должен существовать перед обновлением");
        ApplicantModel saved = savedOpt.get();

        ApplicantModel updated =
                new ApplicantModel(
                        saved.getId(),
                        "UpdatedName",
                        saved.getEmail(),
                        saved.getPasswordHash(),
                        saved.getAddress(),
                        saved.getPhoneNumber(),
                        saved.getLocationId());

        ApplicantModel result = applicantRepository.updateApplicant(updated);

        assertNotNull(result);
        assertEquals("UpdatedName", result.getName());

        var fromDbOpt = applicantRepository.getApplicantById(id);
        assertTrue(fromDbOpt.isPresent());
        assertEquals("UpdatedName", fromDbOpt.get().getName());
    }

    @Test
    void deleteApplicant_whenApplicantExists_thenReturnTrueAndNotFound() {
        long id = 3;
        var existingOpt = applicantRepository.getApplicantById(id);
        assertTrue(
                existingOpt.isPresent(),
                "предзагруженный кандидат должен существовать перед удалением");

        Boolean deleted = applicantRepository.deleteApplicant(id);

        assertTrue(deleted);
        var fromDbOpt = applicantRepository.getApplicantById(id);
        assertFalse(fromDbOpt.isPresent());
    }

    @Test
    void getApplicantById_whenMissing_thenNullReturned() {
        long missingId = 9999L;
        var fromDbOpt = applicantRepository.getApplicantById(missingId);
        assertFalse(fromDbOpt.isPresent(), "должно вернуться null для несуществующего id");
    }

    @Test
    void getApplicantByEmail_whenMissing_thenNullReturned() {
        String missingEmail = "noone@example.com";
        var fromDbOpt = applicantRepository.getApplicantByEmail(missingEmail);
        assertFalse(fromDbOpt.isPresent(), "должно вернуться null для несуществующего email");
    }

    @Test
    void createApplicant_whenDuplicateEmail_thenThrowsDataIntegrityViolation() {
        ApplicantModel duplicate = buildApplicantModel("alice@example.com");

        assertThrows(
                DataIntegrityViolationException.class,
                () -> applicantRepository.createApplicant(duplicate),
                "создание кандидата с дубликатным email должно вызывать ошибку целостности");
    }

    private ApplicantModel buildApplicantModel(String email) {
        return new ApplicantModel(null, "First", email, "passHash", "Address", "+70000000000", 1L);
    }
}
