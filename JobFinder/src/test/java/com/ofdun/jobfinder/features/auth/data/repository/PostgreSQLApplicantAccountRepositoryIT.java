package com.ofdun.jobfinder.features.auth.data.repository;

import static org.junit.jupiter.api.Assertions.*;

import com.ofdun.jobfinder.features.applicant.data.repository.PostgreSQLApplicantRepository;
import com.ofdun.jobfinder.features.auth.domain.model.ApplicantAccountModel;
import com.ofdun.jobfinder.features.auth.domain.repository.ApplicantAccountRepository;
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
@Import({PostgreSQLApplicantAccountRepository.class, PostgreSQLApplicantRepository.class})
@Testcontainers
class PostgreSQLApplicantAccountRepositoryIT {

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

    @Autowired private ApplicantAccountRepository applicantAccountRepository;

    @Test
    void findByEmail_whenExists_thenReturnAccountModel() {
        ApplicantAccountModel account = applicantAccountRepository.findByEmail("alice@example.com");

        assertNotNull(account);
        assertEquals(1L, account.getId());
        assertEquals("alice@example.com", account.getEmail());
        assertEquals("passHash", account.getPasswordHash());
    }

    @Test
    void findByEmail_whenMissing_thenReturnNull() {
        ApplicantAccountModel account =
                applicantAccountRepository.findByEmail("missing@example.com");
        assertNull(account);
    }
}
