package com.ofdun.jobfinder.features.location.data.repository;

import static org.junit.jupiter.api.Assertions.*;

import com.ofdun.jobfinder.features.location.domain.repository.LocationRepository;
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
@Import(PostgreSQLLocationRepository.class)
@Testcontainers
class PostgreSQLLocationRepositoryIT {

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

    @Autowired private LocationRepository locationRepository;

    @Test
    void getLocationById_whenExists_thenReturnLocation() {
        long id = 1L;

        var fromDbOpt = locationRepository.getLocationById(id);

        assertTrue(fromDbOpt.isPresent());
        assertEquals(id, fromDbOpt.get().getId());
        assertEquals("Vila", fromDbOpt.get().getCity());
        assertEquals("Andorra", fromDbOpt.get().getCountry());
    }

    @Test
    void getLocationById_whenMissing_thenEmptyReturned() {
        long missingId = 9999L;

        var fromDbOpt = locationRepository.getLocationById(missingId);

        assertFalse(fromDbOpt.isPresent());
    }

    @Test
    void getLocationById_whenNullId_thenThrowsException() {
        assertThrows(RuntimeException.class, () -> locationRepository.getLocationById(null));
    }
}
