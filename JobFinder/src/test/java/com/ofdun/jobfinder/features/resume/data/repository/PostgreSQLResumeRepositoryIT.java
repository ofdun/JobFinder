package com.ofdun.jobfinder.features.resume.data.repository;

import static org.junit.jupiter.api.Assertions.*;

import com.ofdun.jobfinder.features.resume.domain.model.ResumeModel;
import com.ofdun.jobfinder.features.resume.domain.repository.RelationalResumeRepository;
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
                        1L,
                        "New resume description",
                        null,
                        null,
                        null,
                        null,
                        new java.util.Date());

        Long id = resumeRepository.createResume(model);

        assertNotNull(id);

        var fromDbOpt = resumeRepository.getResumeById(id);
        assertTrue(fromDbOpt.isPresent());
        assertEquals("New resume description", fromDbOpt.get().getDescription());
    }

    @Test
    void getResumeById_whenExists_thenReturnResume() {
        long id = 1L;

        var fromDbOpt = resumeRepository.getResumeById(id);

        assertTrue(fromDbOpt.isPresent());
        assertEquals(id, fromDbOpt.get().getId());
    }

    @Test
    void updateResume_whenExists_thenFieldsUpdated() {
        long id = 2L;
        var savedOpt = resumeRepository.getResumeById(id);
        assertTrue(savedOpt.isPresent());
        ResumeModel saved = savedOpt.get();

        ResumeModel updated =
                new ResumeModel(
                        saved.getId(),
                        saved.getApplicantId(),
                        saved.getCategoryId(),
                        "Updated description",
                        saved.getSkillIds(),
                        saved.getEducations(),
                        saved.getJobExperiences(),
                        saved.getLanguageIds(),
                        saved.getDate());

        ResumeModel result = resumeRepository.updateResume(updated);

        assertNotNull(result);
        assertEquals("Updated description", result.getDescription());

        var fromDbOpt = resumeRepository.getResumeById(id);
        assertTrue(fromDbOpt.isPresent());
        assertEquals("Updated description", fromDbOpt.get().getDescription());
    }

    @Test
    void deleteResume_whenExists_thenReturnTrueAndNotFound() {
        long id = 2L;
        var existingOpt = resumeRepository.getResumeById(id);
        assertTrue(existingOpt.isPresent());

        Boolean deleted = resumeRepository.deleteResume(id);

        assertTrue(deleted);
        assertTrue(resumeRepository.getResumeById(id).isEmpty());
    }

    @Test
    void getResumeById_whenMissing_thenNullReturned() {
        long missingId = 9999L;
        assertTrue(resumeRepository.getResumeById(missingId).isEmpty());
    }
}
