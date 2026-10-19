package com.ofdun.jobfinder.features.resume.data.repository;

import static org.junit.jupiter.api.Assertions.*;

import com.ofdun.jobfinder.features.resume.data.postgres.repository.PostgreSQLResumeRepository;
import com.ofdun.jobfinder.features.resume.domain.model.ResumeModel;
import com.ofdun.jobfinder.features.resume.domain.repository.RelationalResumeRepository;
import org.junit.jupiter.api.Tag;
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

@Tag("postgres")
@ActiveProfiles("test-postgres")
@DataJpaTest
@org.springframework.transaction.annotation.Transactional
@Import(PostgreSQLResumeRepository.class)
@Testcontainers
class PostgreSQLResumeRepositoryIT implements ResumePersistenceContract {

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

    @jakarta.persistence.PersistenceContext private jakarta.persistence.EntityManager entityManager;
    @Autowired private org.springframework.jdbc.core.JdbcTemplate jdbc;

    @Override
    public RelationalResumeRepository repository() {
        return resumeRepository;
    }

    @Override
    public void flushAndClear() {
        entityManager.flush();
        entityManager.clear();
    }

    @Override
    public void assertNoChildren(long id) {
        assertEquals(
                0L,
                jdbc.queryForObject(
                        "select count(*) from jobfinder.educations where resume_id=?",
                        Long.class,
                        id));
        assertEquals(
                0L,
                jdbc.queryForObject(
                        "select count(*) from jobfinder.experiences where resume_id=?",
                        Long.class,
                        id));
    }

    @Test
    @Override
    public void nestedRecordsRoundTripWithCurrentEmployment() {
        ResumePersistenceContract.super.nestedRecordsRoundTripWithCurrentEmployment();
    }

    @Test
    @Override
    public void replacingWithEmptyCollectionsRemovesStoredRelations() {
        ResumePersistenceContract.super.replacingWithEmptyCollectionsRemovesStoredRelations();
    }

    @Test
    @Override
    public void replacingChildrenDoesNotAppendOrModifyAnotherResume() {
        ResumePersistenceContract.super.replacingChildrenDoesNotAppendOrModifyAnotherResume();
    }

    @Test
    @Override
    public void deletingResumeRemovesItsChildrenOnly() {
        ResumePersistenceContract.super.deletingResumeRemovesItsChildrenOnly();
    }

    @Test
    @Override
    public void paginationWithEqualDatesHasStableOrderAndFullCount() {
        ResumePersistenceContract.super.paginationWithEqualDatesHasStableOrderAndFullCount();
    }

    @Test
    @Override
    public void ownershipFilterCannotReturnOtherApplicantsResumes() {
        ResumePersistenceContract.super.ownershipFilterCannotReturnOtherApplicantsResumes();
    }
}
