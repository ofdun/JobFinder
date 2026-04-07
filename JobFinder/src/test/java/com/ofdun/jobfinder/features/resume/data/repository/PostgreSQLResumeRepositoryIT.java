package com.ofdun.jobfinder.features.resume.data.repository;

import static org.junit.jupiter.api.Assertions.*;

import com.ofdun.jobfinder.features.resume.domain.model.ResumeModel;
import com.ofdun.jobfinder.features.resume.domain.repository.RelationalResumeRepository;
import com.ofdun.jobfinder.shared.category.model.CategoryModel;
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
@Import(PostgreSQLResumeRepository.class)
@Testcontainers
class PostgreSQLResumeRepositoryIT {

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

    @Autowired private RelationalResumeRepository resumeRepository;

    @Test
    void createResume_whenValid_thenIdReturned() {
        ResumeModel model =
                new ResumeModel(
                        null,
                        1L,
                        new CategoryModel(1L, "Software Development"),
                        "New resume description",
                        null,
                        null,
                        null,
                        null,
                        new java.util.Date());

        Long id = resumeRepository.createResume(model);

        assertNotNull(id);

        ResumeModel fromDb = resumeRepository.getResumeById(id);
        assertNotNull(fromDb);
        assertEquals("New resume description", fromDb.getDescription());
    }

    @Test
    void getResumeById_whenExists_thenReturnResume() {
        long id = 1L;

        ResumeModel fromDb = resumeRepository.getResumeById(id);

        assertNotNull(fromDb);
        assertEquals(id, fromDb.getId());
    }

    @Test
    void updateResume_whenExists_thenFieldsUpdated() {
        long id = 2L;
        ResumeModel saved = resumeRepository.getResumeById(id);
        assertNotNull(saved);

        ResumeModel updated =
                new ResumeModel(
                        saved.getId(),
                        saved.getApplicantId(),
                        saved.getCategory(),
                        "Updated description",
                        saved.getSkills(),
                        saved.getEducations(),
                        saved.getJobExperiences(),
                        saved.getLanguages(),
                        saved.getDate());

        ResumeModel result = resumeRepository.updateResume(updated);

        assertNotNull(result);
        assertEquals("Updated description", result.getDescription());

        ResumeModel fromDb = resumeRepository.getResumeById(id);
        assertEquals("Updated description", fromDb.getDescription());
    }

    @Test
    void deleteResume_whenExists_thenReturnTrueAndNotFound() {
        long id = 2L;
        ResumeModel existing = resumeRepository.getResumeById(id);
        assertNotNull(existing);

        Boolean deleted = resumeRepository.deleteResume(id);

        assertTrue(deleted);
        ResumeModel fromDb = resumeRepository.getResumeById(id);
        assertNull(fromDb);
    }

    @Test
    void getResumeById_whenMissing_thenNullReturned() {
        long missingId = 9999L;
        ResumeModel fromDb = resumeRepository.getResumeById(missingId);
        assertNull(fromDb);
    }
}
