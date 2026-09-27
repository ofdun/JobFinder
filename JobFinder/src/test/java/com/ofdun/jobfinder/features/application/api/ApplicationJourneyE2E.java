package com.ofdun.jobfinder.features.application.api;

import static org.junit.jupiter.api.Assertions.*;

import org.flywaydb.core.Flyway;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.crypto.bcrypt.BCrypt;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.MongoDBContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import tools.jackson.databind.JsonNode;
import tools.jackson.databind.json.JsonMapper;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Duration;
import java.util.UUID;

@SpringBootTest(
        webEnvironment = SpringBootTest.WebEnvironment.DEFINED_PORT,
        properties = {
            "server.port=18080",
            "spring.flyway.clean-disabled=false",
            "app.qdrant.collection-name=acceptance",
            "app.qdrant.embedding-dimension=3"
        })
@ActiveProfiles("test-postgres")
@Testcontainers
@Tag("e2e")
@Tag("state-transition")
class ApplicationJourneyE2E {
    static final String signingKey = UUID.randomUUID().toString() + UUID.randomUUID();

    @Container
    static final GenericContainer<?> postgres =
            new GenericContainer<>("postgres:15-alpine")
                    .withExposedPorts(5432)
                    .withEnv("POSTGRES_DB", "test")
                    .withEnv("POSTGRES_USER", "test")
                    .withEnv("POSTGRES_PASSWORD", "test");

    @Container
    static final GenericContainer<?> redis =
            new GenericContainer<>("redis:7-alpine").withExposedPorts(6379);

    @Container
    static final GenericContainer<?> qdrant =
            new GenericContainer<>("qdrant/qdrant:v1.17.0").withExposedPorts(6333, 6334);

    @Container static final MongoDBContainer mongo = new MongoDBContainer("mongo:7.0");

    @DynamicPropertySource
    static void properties(DynamicPropertyRegistry registry) {
        registry.add("app.jwt.secret", () -> signingKey);
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
        registry.add("spring.data.redis.host", redis::getHost);
        registry.add("spring.data.redis.port", () -> redis.getMappedPort(6379));
        registry.add("spring.mongodb.uri", mongo::getReplicaSetUrl);
        registry.add("spring.data.mongodb.uri", mongo::getReplicaSetUrl);
        registry.add("app.qdrant.base", qdrant::getHost);
        registry.add("app.qdrant.grpc-port", () -> qdrant.getMappedPort(6334));
        registry.add("app.qdrant.rest-port", () -> qdrant.getMappedPort(6333));
    }

    @Autowired JdbcTemplate jdbc;
    @Autowired Flyway flyway;

    final JsonMapper json = JsonMapper.builder().build();
    final HttpClient http = HttpClient.newBuilder().connectTimeout(Duration.ofSeconds(10)).build();
    final String base = "http://localhost:18080/api/v1";

    @BeforeEach
    void arrangeAccountsAndAvailableVacancy() {
        String hash = BCrypt.hashpw("test-password", BCrypt.gensalt());
        jdbc.update("update jobfinder.applicants set password_hash = ? where id = 1", hash);
        jdbc.update("update jobfinder.employers set password_hash = ? where id = 1", hash);
        jdbc.update("delete from jobfinder.applications where resume_id = 1");
    }

    @Test
    void applicantApplies_employerInvites_applicantSeesInvitation() throws Exception {
        String applicant = login("applicant", "alice@example.com");
        String employer = login("employer", "employer@example.com");
        assertTrue(
                Integer.parseInt(redis.execInContainer("redis-cli", "DBSIZE").getStdout().trim())
                        > 0);

        assertEquals(1L, request("GET", "/vacancies/1", null, null, 200).get("id").asLong());
        String payload = "{\"vacancyId\":1,\"resumeId\":1,\"applicationStatus\":\"NEW\"}";
        long id = request("POST", "/applications", applicant, payload, 201).asLong();
        assertTrue(id > 0);
        assertEquals(
                "NEW",
                request("GET", "/applications/" + id, applicant, null, 200)
                        .get("applicationStatus")
                        .asText());
        assertNotEquals(
                "e2e",
                System.getenv("CI_FORCE_FAILURE"),
                "Requested E2E failure after persisting an application and sessions");

        request("PUT", "/applications/" + id, employer, payload.replace("NEW", "INVITATION"), 200);
        assertEquals(
                "INVITATION",
                request("GET", "/applications/" + id, applicant, null, 200)
                        .get("applicationStatus")
                        .asText());
        assertEquals(
                "INVITATION",
                jdbc.queryForObject(
                        "select status::text from jobfinder.applications where id = ?",
                        String.class,
                        id));
        request("GET", "/applications/" + id, null, null, 403);
        request("DELETE", "/applications/" + id, applicant, null, 204);

        Files.createDirectories(Path.of("build", "traffic"));
        var replay =
                new ProcessBuilder("bash", "../ci/replay.sh", base)
                        .redirectErrorStream(true)
                        .redirectOutput(Path.of("build", "traffic", "curl-replay.log").toFile())
                        .start();
        boolean completed = replay.waitFor(90, java.util.concurrent.TimeUnit.SECONDS);
        if (!completed) replay.destroyForcibly();
        assertTrue(completed, "curl replay timed out");
        assertEquals(0, replay.exitValue(), "See build/traffic/curl-replay.log");
    }

    String login(String role, String email) throws Exception {
        return request(
                        "POST",
                        "/auth/" + role + "/login",
                        null,
                        "{\"email\":\"" + email + "\",\"password\":\"test-password\"}",
                        200)
                .get("accessToken")
                .asText();
    }

    JsonNode request(String method, String path, String token, String payload, int expected)
            throws Exception {
        var request =
                HttpRequest.newBuilder(URI.create(base + path)).timeout(Duration.ofSeconds(15));
        if (token != null) request.header("Authorization", "Bearer " + token);
        if (payload != null) request.header("Content-Type", "application/json");
        request.method(
                method,
                payload == null
                        ? HttpRequest.BodyPublishers.noBody()
                        : HttpRequest.BodyPublishers.ofString(payload));
        var response = http.send(request.build(), HttpResponse.BodyHandlers.ofString());
        assertEquals(expected, response.statusCode(), method + " " + path + ": " + response.body());
        return response.body().isBlank() ? json.nullNode() : json.readTree(response.body());
    }

    @AfterEach
    void restoreFixturesAndTerminateSessions() throws Exception {
        try {
            flyway.clean();
            flyway.migrate();
            assertEquals(
                    2,
                    jdbc.queryForObject(
                            "select count(*) from jobfinder.applications", Integer.class));
            assertEquals(
                    "passHash",
                    jdbc.queryForObject(
                            "select password_hash from jobfinder.applicants where id = 1",
                            String.class));
            System.out.println("FIXTURES_RESTORED applications=2 original_password_hash=passHash");
        } finally {
            assertEquals(0, redis.execInContainer("redis-cli", "FLUSHDB").getExitCode());
            assertEquals("0", redis.execInContainer("redis-cli", "DBSIZE").getStdout().trim());
            System.out.println("SESSIONS_CLEARED redis_keys=0");
        }
    }
}
