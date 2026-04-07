package com.ofdun.jobfinder.features.vacancy.data.repository;

import static org.junit.jupiter.api.Assertions.*;

import com.ofdun.jobfinder.features.vacancy.domain.model.VacancyModel;
import com.ofdun.jobfinder.features.vacancy.domain.repository.VacancyRepository;
import com.ofdun.jobfinder.shared.location.model.LocationModel;
import com.ofdun.jobfinder.shared.vacancy.enums.EmploymentType;
import com.ofdun.jobfinder.shared.vacancy.enums.JobFormat;
import com.ofdun.jobfinder.shared.vacancy.enums.PaymentFrequency;
import java.math.BigDecimal;
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
@Import(PostgreSQLVacancyRepository.class)
@Testcontainers
class PostgreSQLVacancyRepositoryIT {

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

    @Autowired private VacancyRepository vacancyRepository;

    @Test
    void getVacancyById_whenExists_thenReturnVacancy() {
        long id = 1L;

        VacancyModel fromDb = vacancyRepository.getVacancyById(id);

        assertNotNull(fromDb);
        assertEquals(id, fromDb.getId());
        assertEquals("Test vacancy", fromDb.getDescription());
    }

    @Test
    void createVacancy_whenValid_thenIdReturnedAndFound() {
        VacancyModel model =
                new VacancyModel(
                        null,
                        1L,
                        new LocationModel(1L, "City", "Country"),
                        new BigDecimal("2000"),
                        null,
                        null,
                        PaymentFrequency.MONTHLY,
                        "5 years",
                        JobFormat.REMOTE,
                        EmploymentType.FULL_TIME,
                        "New vacancy",
                        new java.util.Date(),
                        "Address");

        Long id = vacancyRepository.createVacancy(model);

        assertNotNull(id);

        VacancyModel fromDb = vacancyRepository.getVacancyById(id);
        assertNotNull(fromDb);
        assertEquals(new BigDecimal("2000"), fromDb.getSalary());
    }

    @Test
    void updateVacancy_whenExists_thenFieldsUpdated() {
        long id = 1L;
        VacancyModel existing = vacancyRepository.getVacancyById(id);
        assertNotNull(existing);

        VacancyModel updated =
                new VacancyModel(
                        existing.getId(),
                        existing.getEmployerId(),
                        existing.getLocation(),
                        existing.getSalary(),
                        existing.getSkills(),
                        existing.getLanguages(),
                        existing.getPaymentFrequency(),
                        existing.getExperience(),
                        existing.getJobFormat(),
                        existing.getEmploymentType(),
                        "Updated description",
                        existing.getPublicationDate(),
                        existing.getAddress());

        VacancyModel result = vacancyRepository.updateVacancy(updated);

        assertNotNull(result);
        assertEquals("Updated description", result.getDescription());

        VacancyModel fromDb = vacancyRepository.getVacancyById(id);
        assertEquals("Updated description", fromDb.getDescription());
    }

    @Test
    void deleteVacancy_whenExists_thenReturnTrueAndNotFound() {
        long id = 1L;
        VacancyModel existing = vacancyRepository.getVacancyById(id);
        assertNotNull(existing);

        Boolean deleted = vacancyRepository.deleteVacancy(id);

        assertTrue(deleted);
        VacancyModel fromDb = vacancyRepository.getVacancyById(id);
        assertNull(fromDb);
    }

    @Test
    void getVacancyById_whenMissing_thenNullReturned() {
        long missingId = 9999L;
        VacancyModel fromDb = vacancyRepository.getVacancyById(missingId);
        assertNull(fromDb);
    }
}
