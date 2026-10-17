package com.ofdun.jobfinder.features.application.api;

import static org.junit.jupiter.api.Assertions.*;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Duration;
import java.util.UUID;
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
    @Autowired com.zaxxer.hikari.HikariDataSource dataSource;

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
                HttpRequest.newBuilder(
                                URI.create(
                                        path.startsWith("/api/")
                                                ? "http://localhost:18080" + path
                                                : base + path))
                        .timeout(Duration.ofSeconds(15));
        if (path.equals("/api/v2")) request.header("Accept", "application/graphql-response+json");
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
            // Recreated PostgreSQL enum types invalidate cached prepared statements.
            dataSource.getHikariPoolMXBean().softEvictConnections();
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

    @Test
    void unifiedAuthenticationRotatesRefreshAndRejectsWrongRoleAndReplay() throws Exception {
        for (String role : new String[] {"APPLICANT", "EMPLOYER"}) {
            String email = role.equals("APPLICANT") ? "alice@example.com" : "employer@example.com";
            var tokens = loginV2(role, email);
            String refresh = tokens.get("refreshToken").asText();
            String wrongRole = role.equals("APPLICANT") ? "EMPLOYER" : "APPLICANT";
            request("POST", "/api/v2/auth/refresh", null, refreshBody(wrongRole, refresh), 401);
            var rotated =
                    request("POST", "/api/v2/auth/refresh", null, refreshBody(role, refresh), 200);
            assertNotEquals(refresh, rotated.get("refreshToken").asText());
            request("POST", "/api/v2/auth/refresh", null, refreshBody(role, refresh), 401);
            request(
                    "POST",
                    "/api/v2/auth/refresh",
                    null,
                    refreshBody(role, tokens.get("accessToken").asText()),
                    401);
            String profile = role.equals("APPLICANT") ? "/applicants/1" : "/employers/1";
            request("GET", "/api/v2" + profile, rotated.get("accessToken").asText(), null, 200);
            request("GET", "/api/v2" + profile, rotated.get("refreshToken").asText(), null, 401);
        }
    }

    @Test
    void v2ApplicationStatusIsIdempotentAndOnlyEmployerCanChangeIt() throws Exception {
        String applicant = loginV2("APPLICANT", "alice@example.com").get("accessToken").asText();
        String employer = loginV2("EMPLOYER", "employer@example.com").get("accessToken").asText();
        String body = "{\"vacancyId\":1,\"resumeId\":1,\"applicationStatus\":\"NEW\"}";
        long id = request("POST", "/api/v2/applications", applicant, body, 201).asLong();
        request("POST", "/api/v2/applications", applicant, body, 409);
        var before = request("GET", "/api/v2/applications/" + id, applicant, null, 200);
        String statusPath = "/api/v2/applications/" + id + "/status";
        String invitation = "{\"applicationStatus\":\"INVITATION\"}";
        request("PUT", statusPath, applicant, invitation, 403);
        request("PUT", statusPath, null, invitation, 401);
        request("PUT", statusPath, employer, "{\"applicationStatus\":\"UNKNOWN\"}", 400);
        for (int attempt = 0; attempt < 2; attempt++) {
            var after = request("PUT", statusPath, employer, invitation, 200);
            assertEquals("INVITATION", after.get("applicationStatus").asText());
            for (String field : new String[] {"id", "vacancyId", "resumeId", "applicationDate"}) {
                assertNotNull(before.get(field), field);
                assertEquals(before.get(field), after.get(field), field);
            }
        }
        assertEquals(
                "INVITATION",
                jdbc.queryForObject(
                        "select status::text from jobfinder.applications where id=?",
                        String.class,
                        id));
        var graphql =
                request(
                        "POST",
                        "/api/v2",
                        applicant,
                        "{\"operationName\":\"ReadApplication\",\"query\":\"query"
                                + " ReadApplication($id: ID!) { application(id: $id) { id"
                                + " applicationStatus } }\",\"variables\":{\"id\":\""
                                + id
                                + "\"}}",
                        200);
        assertFalse(graphql.has("errors"), graphql.toString());
        assertEquals("INVITATION", graphql.at("/data/application/applicationStatus").asText());
        assertEquals(id, graphql.at("/data/application/id").asLong());
        request("DELETE", "/api/v2/applications/" + id, applicant, null, 204);
    }

    @Test
    void canonicalDraftRestoreRestoresSnapshotAndChecksRole() throws Exception {
        String employer = loginV2("EMPLOYER", "employer@example.com").get("accessToken").asText();
        String applicant = loginV2("APPLICANT", "alice@example.com").get("accessToken").asText();
        var original = request("GET", "/api/v2/vacancies/1", null, null, 200);
        long draft =
                request("POST", "/api/v2/vacancies/1/drafts", employer, null, 201)
                        .get("id")
                        .asLong();
        jdbc.update(
                "update jobfinder.vacancies set description='changed', status='INACTIVE' where"
                        + " id=1");
        String path = "/api/v2/vacancies/1/drafts/" + draft;
        request("POST", path, applicant, null, 403);
        assertEquals(
                "changed",
                jdbc.queryForObject(
                        "select description from jobfinder.vacancies where id=1", String.class));
        var restored = request("POST", path, employer, null, 200);
        assertEquals(original.get("description"), restored.get("description"));
        assertEquals("ACTIVE", restored.get("status").asText());
        request("DELETE", path, employer, null, 204);
        request("POST", path, employer, null, 404);
    }

    JsonNode loginV2(String role, String email) throws Exception {
        return request(
                "POST",
                "/api/v2/auth/login",
                null,
                "{\"email\":\""
                        + email
                        + "\",\"password\":\"test-password\",\"userType\":\""
                        + role
                        + "\"}",
                200);
    }

    String refreshBody(String role, String token) {
        return "{\"userType\":\"" + role + "\",\"refreshToken\":\"" + token + "\"}";
    }
}
