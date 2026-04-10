package com.ofdun.jobfinder.features.employer.data.repository;

import static org.junit.jupiter.api.Assertions.*;

import com.ofdun.jobfinder.features.employer.domain.model.EmployerModel;
import com.ofdun.jobfinder.features.employer.domain.repository.EmployerRepository;
import jakarta.validation.ConstraintViolationException;
import java.util.Optional;
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
@Import(PostgreSQLEmployerRepository.class)
@Testcontainers
class PostgreSQLEmployerRepositoryIT {

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

    @Autowired private EmployerRepository employerRepository;

    @BeforeEach
    void setUp() {}

    @Test
    void getEmployerById_whenExists_thenReturnModel() {
        Optional<EmployerModel> employerOpt = employerRepository.getEmployerById(1L);

        assertTrue(employerOpt.isPresent());
        EmployerModel employer = employerOpt.orElseThrow();
        assertEquals(1L, employer.getId());
        assertEquals("Test Employer", employer.getName());
        assertEquals("employer@example.com", employer.getEmail());
    }

    @Test
    void getEmployerById_whenMissing_thenReturnEmpty() {
        Optional<EmployerModel> employerOpt = employerRepository.getEmployerById(999L);
        assertTrue(employerOpt.isEmpty());
    }

    @Test
    void getEmployerByEmail_whenExists_thenReturnModel() {
        Optional<EmployerModel> employerOpt = employerRepository.getEmployerByEmail("employer@example.com");
        assertTrue(employerOpt.isPresent());
        EmployerModel employer = employerOpt.orElseThrow();
        assertEquals(1L, employer.getId());
        assertEquals("Test Employer", employer.getName());
    }

    @Test
    void createEmployer_thenCanBeRetrieved() {
        EmployerModel newEmployer =
                new EmployerModel(
                        null,
                        "New Co",
                        "hash",
                        null,
                        "Addr",
                        "https://newco.example",
                        "newco@example.com",
                        1L);

        Long id = employerRepository.createEmployer(newEmployer);
        Optional<EmployerModel> createdOpt = employerRepository.getEmployerById(id);

        assertTrue(createdOpt.isPresent());
        EmployerModel created = createdOpt.orElseThrow();
        assertEquals("New Co", created.getName());
        assertEquals("newco@example.com", created.getEmail());
    }

    @Test
    void createEmployer_whenMissingRequired_thenThrow() {
        EmployerModel newEmployer = new EmployerModel(null, "", "", null, null, "", "", 1L);

        assertThrows(
                ConstraintViolationException.class,
                () -> employerRepository.createEmployer(newEmployer));
    }

    @Test
    void updateEmployer_thenPersistChanges() {
        EmployerModel employer = employerRepository.getEmployerById(1L).orElseThrow();
        employer.setName("Updated Name");
        employer.setWebsiteLink("https://updated.example");

        EmployerModel updated = employerRepository.updateEmployer(employer);

        assertNotNull(updated);
        assertEquals(1L, updated.getId());
        assertEquals("Updated Name", updated.getName());
        assertEquals("https://updated.example", updated.getWebsiteLink());
    }

    @Test
    void deleteEmployer_whenExists_thenReturnTrue() {
        Boolean deleted = employerRepository.deleteEmployer(1L);
        assertTrue(deleted);
        assertTrue(employerRepository.getEmployerById(1L).isEmpty());
    }

    @Test
    void deleteEmployer_whenMissing_thenReturnFalse() {
        Boolean deleted = employerRepository.deleteEmployer(999L);
        assertFalse(deleted);
    }
}
