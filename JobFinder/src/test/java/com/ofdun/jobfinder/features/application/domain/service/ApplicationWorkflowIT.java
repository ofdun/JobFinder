package com.ofdun.jobfinder.features.application.domain.service;

import static org.junit.jupiter.api.Assertions.*;

import com.ofdun.jobfinder.features.application.data.postgres.repository.PostgreSQLApplicationRepository;
import com.ofdun.jobfinder.features.application.domain.model.ApplicationModel;
import com.ofdun.jobfinder.features.application.domain.validator.BasicApplicationValidator;
import com.ofdun.jobfinder.features.application.enums.ApplicationStatus;
import com.ofdun.jobfinder.features.application.exception.ApplicationNotFoundException;

import jakarta.persistence.EntityManager;

import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.context.annotation.Import;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.context.transaction.AfterTransaction;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.util.Date;

@DataJpaTest
@ActiveProfiles("test-postgres")
@Import({
    BasicApplicationService.class,
    BasicApplicationValidator.class,
    PostgreSQLApplicationRepository.class
})
@Testcontainers
@Tag("postgres")
@Tag("state-transition")
class ApplicationWorkflowIT {
    @Container
    static final GenericContainer<?> postgres =
            new GenericContainer<>("postgres:15-alpine")
                    .withExposedPorts(5432)
                    .withEnv("POSTGRES_DB", "test")
                    .withEnv("POSTGRES_USER", "test")
                    .withEnv("POSTGRES_PASSWORD", "test");

    @DynamicPropertySource
    static void properties(DynamicPropertyRegistry registry) {
        registry.add(
                "spring.datasource.url",
                () ->
                        "jdbc:postgresql://"
                                + postgres.getHost()
                                + ":"
                                + postgres.getMappedPort(5432)
                                + "/test");
        registry.add("spring.datasource.username", () -> "test");
        registry.add("spring.datasource.password", () -> "test");
    }

    @Autowired ApplicationService service;
    @Autowired JdbcTemplate jdbc;
    @Autowired EntityManager entityManager;

    @Test
    void applicantApplies_employerInvites_applicationIsWithdrawn() {
        var application = new ApplicationModel(null, 1L, 1L, new Date(), ApplicationStatus.NEW);

        Long id = service.saveApplication(application);
        assertNotEquals(
                "integration",
                System.getenv("CI_FORCE_FAILURE"),
                "Requested integration failure after persisting an application");

        assertEquals(ApplicationStatus.NEW, service.getApplication(id).getApplicationStatus());

        application.setId(id);
        application.setApplicationStatus(ApplicationStatus.INVITATION);
        service.updateApplication(application);
        entityManager.flush();
        entityManager.clear();

        assertEquals(
                ApplicationStatus.INVITATION, service.getApplication(id).getApplicationStatus());
        assertEquals(
                "INVITATION",
                jdbc.queryForObject(
                        "select status::text from jobfinder.applications where id = ?",
                        String.class,
                        id));

        assertTrue(service.deleteApplication(id));
        assertThrows(ApplicationNotFoundException.class, () -> service.getApplication(id));
        assertThrows(ApplicationNotFoundException.class, () -> service.deleteApplication(id));
        assertThrows(
                IllegalArgumentException.class,
                () ->
                        service.saveApplication(
                                new ApplicationModel(
                                        null, 0L, 1L, new Date(), ApplicationStatus.NEW)));
    }

    @AfterTransaction
    void verifyRollback() {
        assertEquals(
                2,
                jdbc.queryForObject("select count(*) from jobfinder.applications", Integer.class));
        assertEquals(
                "NEW",
                jdbc.queryForObject(
                        "select status::text from jobfinder.applications where id = 1",
                        String.class));
        System.out.println("ROLLBACK_VERIFIED applications=2 initial_status=NEW");
    }
}
