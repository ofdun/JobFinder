package com.ofdun.jobfinder.features.category.data.repository;

import static org.junit.jupiter.api.Assertions.*;

import com.ofdun.jobfinder.features.category.domain.repository.CategoryRepository;
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
@Import(PostgreSQLCategoryRepository.class)
@Testcontainers
class PostgreSQLCategoryRepositoryIT {

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

    @Autowired private CategoryRepository categoryRepository;

    @Test
    void getCategoryById_whenExists_thenReturnCategory() {
        long id = 1L;

        var fromDbOpt = categoryRepository.getCategoryById(id);

        assertTrue(fromDbOpt.isPresent());
        assertEquals(id, fromDbOpt.get().getId());
        assertEquals("Software Development", fromDbOpt.get().getName());
    }

    @Test
    void getCategoryById_whenMissing_thenEmptyReturned() {
        long missingId = 9999L;

        var fromDbOpt = categoryRepository.getCategoryById(missingId);

        assertFalse(fromDbOpt.isPresent());
    }

    @Test
    void getCategoryById_whenNullId_thenThrowsException() {
        assertThrows(RuntimeException.class, () -> categoryRepository.getCategoryById(null));
    }
}
