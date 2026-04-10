package com.ofdun.jobfinder.features.application.data.repository;

import static org.junit.jupiter.api.Assertions.*;

import com.ofdun.jobfinder.features.application.domain.model.ApplicationModel;
import com.ofdun.jobfinder.features.application.domain.repository.ApplicationRepository;
import com.ofdun.jobfinder.features.application.enums.ApplicationStatus;
import java.util.Date;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.utility.DockerImageName;

@ActiveProfiles("test")
@DataJpaTest
@Import(PostgreSQLApplicationRepository.class)
@Testcontainers
class PostgreSQLApplicationRepositoryIT {

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

    @Autowired private ApplicationRepository applicationRepository;

    @BeforeEach
    void cleanup() {}

    @Test
    void createApplication_whenValidApplication_thenIdReturned() {
        ApplicationModel model =
                buildApplicationModel(null, 1L, 1L, new Date(), ApplicationStatus.NEW);

        Long id = applicationRepository.createApplication(model);

        assertNotNull(id);

        var fromDbOpt = applicationRepository.getApplicationById(id);
        assertTrue(fromDbOpt.isPresent());
        assertEquals(1L, fromDbOpt.get().getVacancyId());
        assertEquals(1L, fromDbOpt.get().getResumeId());
        assertEquals(ApplicationStatus.NEW, fromDbOpt.get().getApplicationStatus());
    }

    @Test
    void getApplication_whenExists_thenReturnApplication() {
        Long id = 1L;

        var fromDbOpt = applicationRepository.getApplicationById(id);

        assertTrue(fromDbOpt.isPresent());
        assertEquals(id, fromDbOpt.get().getId());
    }

    @Test
    void updateApplication_whenApplicationExists_thenFieldsUpdated() {
        Long id = 1L;
        var savedOpt = applicationRepository.getApplicationById(id);
        assertTrue(savedOpt.isPresent());
        ApplicationModel saved = savedOpt.get();

        ApplicationModel updated =
                new ApplicationModel(
                        saved.getId(),
                        saved.getVacancyId(),
                        saved.getResumeId(),
                        new Date(),
                        ApplicationStatus.INVITATION);

        ApplicationModel result = applicationRepository.updateApplication(updated);

        assertNotNull(result);
        assertEquals(ApplicationStatus.INVITATION, result.getApplicationStatus());

        var fromDbOpt = applicationRepository.getApplicationById(id);
        assertTrue(fromDbOpt.isPresent());
        assertEquals(ApplicationStatus.INVITATION, fromDbOpt.get().getApplicationStatus());
    }

    @Test
    void deleteApplication_whenApplicationExists_thenReturnTrueAndNotFound() {
        Long id = 2L;
        var existingOpt = applicationRepository.getApplicationById(id);
        assertTrue(existingOpt.isPresent());

        Boolean deleted = applicationRepository.deleteApplication(id);

        assertTrue(deleted);
        assertTrue(applicationRepository.getApplicationById(id).isEmpty());
    }

    @Test
    void getApplication_whenMissing_thenNullReturned() {
        Long missingId = 9999L;
        assertTrue(applicationRepository.getApplicationById(missingId).isEmpty());
    }

    private ApplicationModel buildApplicationModel(
            Long id, Long vacancyId, Long resumeId, Date date, ApplicationStatus status) {
        return new ApplicationModel(id, vacancyId, resumeId, date, status);
    }
}
