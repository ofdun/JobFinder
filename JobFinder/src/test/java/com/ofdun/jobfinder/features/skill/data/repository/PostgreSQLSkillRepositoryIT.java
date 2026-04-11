package com.ofdun.jobfinder.features.skill.data.repository;

import static org.junit.jupiter.api.Assertions.*;

import com.ofdun.jobfinder.features.skill.domain.repository.SkillRepository;
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
@Import(PostgreSQLSkillRepository.class)
@Testcontainers
class PostgreSQLSkillRepositoryIT {

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

    @Autowired private SkillRepository skillRepository;

    @Test
    void getSkillById_whenExists_thenReturnSkill() {
        long id = 1L;

        var fromDbOpt = skillRepository.getSkillById(id);

        assertTrue(fromDbOpt.isPresent());
        assertEquals(id, fromDbOpt.get().getId());
        assertEquals("Python", fromDbOpt.get().getName());
    }

    @Test
    void getSkillById_whenMissing_thenEmptyReturned() {
        long missingId = 9999L;

        var fromDbOpt = skillRepository.getSkillById(missingId);

        assertFalse(fromDbOpt.isPresent());
    }

    @Test
    void getSkillById_whenNullId_thenThrowsException() {
        assertThrows(RuntimeException.class, () -> skillRepository.getSkillById(null));
    }
}
