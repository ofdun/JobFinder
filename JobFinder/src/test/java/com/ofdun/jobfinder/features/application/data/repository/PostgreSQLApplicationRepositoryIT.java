package com.ofdun.jobfinder.features.application.data.repository;

import static org.junit.jupiter.api.Assertions.*;

import com.ofdun.jobfinder.features.application.domain.model.ApplicationModel;
import com.ofdun.jobfinder.features.application.domain.repository.ApplicationRepository;
import com.ofdun.jobfinder.shared.application.enums.ApplicationStatus;
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
    void saveApplication_whenValidApplication_thenIdReturned() {
        ApplicationModel model =
                buildApplicationModel(null, 1L, 1L, new Date(), ApplicationStatus.NEW);

        Long id = applicationRepository.saveApplication(model);

        assertNotNull(id);

        ApplicationModel fromDb = applicationRepository.getApplication(id);
        assertNotNull(fromDb);
        assertEquals(1L, fromDb.getVacancyId());
        assertEquals(1L, fromDb.getResumeId());
        assertEquals(ApplicationStatus.NEW, fromDb.getApplicationStatus());
    }

    @Test
    void getApplication_whenExists_thenReturnApplication() {
        Long id = 1L;

        ApplicationModel fromDb = applicationRepository.getApplication(id);

        assertNotNull(fromDb);
        assertEquals(id, fromDb.getId());
    }

    @Test
    void updateApplication_whenApplicationExists_thenFieldsUpdated() {
        Long id = 1L;
        ApplicationModel saved = applicationRepository.getApplication(id);
        assertNotNull(saved);

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

        ApplicationModel fromDb = applicationRepository.getApplication(id);
        assertEquals(ApplicationStatus.INVITATION, fromDb.getApplicationStatus());
    }

    @Test
    void deleteApplication_whenApplicationExists_thenReturnTrueAndNotFound() {
        Long id = 2L;
        ApplicationModel existing = applicationRepository.getApplication(id);
        assertNotNull(existing);

        Boolean deleted = applicationRepository.deleteApplication(id);

        assertTrue(deleted);
        ApplicationModel fromDb = applicationRepository.getApplication(id);
        assertNull(fromDb);
    }

    @Test
    void getApplication_whenMissing_thenNullReturned() {
        Long missingId = 9999L;
        ApplicationModel fromDb = applicationRepository.getApplication(missingId);
        assertNull(fromDb);
    }

    private ApplicationModel buildApplicationModel(
            Long id, Long vacancyId, Long resumeId, Date date, ApplicationStatus status) {
        return new ApplicationModel(id, vacancyId, resumeId, date, status);
    }
}
