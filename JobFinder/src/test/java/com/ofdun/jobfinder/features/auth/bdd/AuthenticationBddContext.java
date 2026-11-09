package com.ofdun.jobfinder.features.auth.bdd;

import io.cucumber.spring.CucumberContextConfiguration;

import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.context.annotation.Primary;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.MongoDBContainer;
import org.testcontainers.containers.PostgreSQLContainer;

import java.security.SecureRandom;
import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.util.Base64;

@CucumberContextConfiguration
@SpringBootTest(
        webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT,
        properties = {
            "management.server.port=0",
            "app.auth.attempt-limit=3",
            "app.qdrant.collection-name=authentication",
            "app.qdrant.embedding-dimension=3"
        })
@ActiveProfiles("test-postgres")
@Import(AuthenticationBddContext.TimeConfiguration.class)
public class AuthenticationBddContext {
    static final String encryptionKey = randomSecret();
    static final PostgreSQLContainer<?> postgres =
            new PostgreSQLContainer<>("postgres:15-alpine")
                    .withDatabaseName("authentication")
                    .withUsername("authentication")
                    .withPassword(randomSecret());
    static final GenericContainer<?> redis =
            new GenericContainer<>("redis:7-alpine").withExposedPorts(6379);
    static final MongoDBContainer mongo = new MongoDBContainer("mongo:7.0");
    static final GenericContainer<?> qdrant =
            new GenericContainer<>("qdrant/qdrant:v1.17.0").withExposedPorts(6333, 6334);

    static String randomSecret() {
        byte[] bytes = new byte[32];
        new SecureRandom().nextBytes(bytes);
        return Base64.getEncoder().encodeToString(bytes);
    }

    @DynamicPropertySource
    static void configure(DynamicPropertyRegistry properties) {
        postgres.start();
        redis.start();
        mongo.start();
        qdrant.start();
        properties.add("spring.datasource.url", postgres::getJdbcUrl);
        properties.add("spring.datasource.username", postgres::getUsername);
        properties.add("spring.datasource.password", postgres::getPassword);
        properties.add("spring.data.redis.host", redis::getHost);
        properties.add("spring.data.redis.port", () -> redis.getMappedPort(6379));
        properties.add("spring.mongodb.uri", mongo::getReplicaSetUrl);
        properties.add("spring.data.mongodb.uri", mongo::getReplicaSetUrl);
        properties.add("app.jwt.secret", () -> encryptionKey);
        properties.add("app.auth.encryption-key", () -> encryptionKey);
        properties.add("app.qdrant.base", qdrant::getHost);
        properties.add("app.qdrant.grpc-port", () -> qdrant.getMappedPort(6334));
        properties.add("app.qdrant.rest-port", () -> qdrant.getMappedPort(6333));
    }

    @TestConfiguration
    static class TimeConfiguration {
        @Bean
        @Primary
        MutableClock testClock() {
            return new MutableClock();
        }
    }

    static class MutableClock extends Clock {
        private Instant instant = Instant.now();

        void reset() {
            instant = Instant.now();
        }

        void advance(Duration duration) {
            instant = instant.plus(duration);
        }

        public ZoneId getZone() {
            return ZoneOffset.UTC;
        }

        public Clock withZone(ZoneId zone) {
            return this;
        }

        public Instant instant() {
            return instant;
        }
    }
}
