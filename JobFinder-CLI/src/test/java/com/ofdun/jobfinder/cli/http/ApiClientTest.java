package com.ofdun.jobfinder.cli.http;

import static org.junit.jupiter.api.Assertions.*;

import com.fasterxml.jackson.databind.JsonNode;
import com.ofdun.jobfinder.cli.CliContext;
import com.ofdun.jobfinder.cli.auth.Session;
import com.ofdun.jobfinder.cli.auth.UserRole;
import com.ofdun.jobfinder.cli.dto.resumes.*;
import com.ofdun.jobfinder.cli.ui.Prompt;
import com.sun.net.httpserver.HttpServer;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class ApiClientTest {
    private HttpServer server;
    private ApiClient client;
    private final LinkedBlockingQueue<Reply> replies = new LinkedBlockingQueue<>();
    private final LinkedBlockingQueue<Call> calls = new LinkedBlockingQueue<>();

    private record Reply(int status, String body) {}

    private record Call(String method, String path, String accept, String auth, JsonNode body) {}

    @BeforeEach
    void start() throws Exception {
        server = HttpServer.create(new InetSocketAddress("127.0.0.1", 0), 0);
        server.createContext(
                "/",
                exchange -> {
                    calls.add(
                            new Call(
                                    exchange.getRequestMethod(),
                                    exchange.getRequestURI().getPath(),
                                    exchange.getRequestHeaders().getFirst("Accept"),
                                    exchange.getRequestHeaders().getFirst("Authorization"),
                                    Json.mapper().readTree(exchange.getRequestBody())));
                    Reply reply = replies.poll();
                    if (reply == null) reply = new Reply(500, "{}");
                    if (reply.status() != 0) {
                        byte[] body = reply.body().getBytes(StandardCharsets.UTF_8);
                        exchange.getResponseHeaders()
                                .add("Content-Type", "application/graphql-response+json");
                        exchange.sendResponseHeaders(reply.status(), body.length);
                        exchange.getResponseBody().write(body);
                    }
                    exchange.close();
                });
        server.start();
        client =
                new ApiClient(
                        new Session(
                                "http://127.0.0.1:" + server.getAddress().getPort() + "/api/v2",
                                UserRole.APPLICANT,
                                "access",
                                "refresh"));
    }

    @AfterEach
    void stop() {
        server.stop(0);
    }

    private void success(String result) {
        replies.add(new Reply(200, "{\"data\":{\"result\":" + result + "}}"));
    }

    private Call next() throws Exception {
        Call call = calls.poll(2, TimeUnit.SECONDS);
        assertNotNull(call);
        return call;
    }

    @Test
    void creationUsesNamedGraphqlOperationAndLosslessIds() throws Exception {
        success("{\"id\":\"9223372036854775807\"}");
        assertEquals(Long.MAX_VALUE, client.createApplication(Long.MAX_VALUE, 20L));
        Call call = next();
        assertEquals("POST", call.method());
        assertEquals("/api/v2", call.path());
        assertEquals("application/graphql-response+json", call.accept());
        assertEquals("Bearer access", call.auth());
        assertEquals("CREATE_APPLICATION", call.body().path("operationName").asText());
        assertEquals(
                "9223372036854775807", call.body().at("/variables/input/vacancyId").textValue());
        assertFalse(call.body().at("/variables/input").has("applicationStatus"));
    }

    @Test
    void resumeDatesAndGraduationYearFollowSchema() throws Exception {
        success("{\"id\":\"20\"}");
        var education =
                new EducationCreate(EducationDegree.MASTER, "University", "IT", "Software", 2026);
        var experience =
                new JobExperienceCreate(
                        "Developer", "Example", null, LocalDate.of(2025, 6, 1), null);
        client.createResume(
                new ResumeRequest(
                        1L,
                        2L,
                        null,
                        List.of(3L),
                        List.of(education),
                        List.of(experience),
                        List.of()));
        JsonNode input = next().body().at("/variables/input");
        assertEquals("2025-06-01", input.at("/jobExperiences/0/startDate").textValue());
        assertTrue(input.at("/jobExperiences/0/endDate").isNull());
        assertEquals(2026, input.at("/educations/0/yearOfGraduation").intValue());
        assertEquals("3", input.at("/skillIds/0").textValue());
    }

    @Test
    void searchSeparatesFiltersPaginationAndSortAndPreservesDecimal() throws Exception {
        success("{\"items\":[],\"page\":1,\"size\":10,\"totalElements\":\"0\",\"totalPages\":0}");
        client.searchVacancies(
                Map.of(
                        "salaryMin",
                        new java.math.BigDecimal("150000.01"),
                        "limit",
                        10,
                        "offset",
                        10,
                        "sortBy",
                        "SALARY",
                        "sortDesc",
                        true));
        Call call = next();
        JsonNode variables = call.body().path("variables");
        assertNull(call.auth());
        assertEquals("150000.01", variables.at("/filter/salaryMin").textValue());
        assertFalse(variables.path("filter").has("limit"));
        assertFalse(variables.path("filter").has("sortBy"));
        assertEquals(10, variables.at("/page/offset").asInt());
        assertEquals("DESC", variables.path("direction").asText());
    }

    @Test
    void transport401RefreshesOnceAndUpdatesSharedSession() throws Exception {
        var context = new CliContext(client.getSession(), client, new Prompt());
        replies.add(new Reply(401, "{\"errors\":[{\"message\":\"expired\"}]}"));
        success("{\"accessToken\":\"new-access\",\"refreshToken\":\"new-refresh\"}");
        success("{\"id\":\"1\",\"name\":\"Anna\"}");
        assertEquals(1L, client.getApplicant(1L).id());
        assertEquals("Bearer access", next().auth());
        Call refresh = next();
        assertNull(refresh.auth());
        assertEquals("REFRESH", refresh.body().path("operationName").asText());
        assertEquals("Bearer new-access", next().auth());
        assertEquals("new-refresh", context.getSession().getRefreshToken());
    }

    @Test
    void graphqlErrorsAt200AreNeverReplayed() throws Exception {
        replies.add(
                new Reply(
                        200,
                        "{\"data\":{\"result\":null},\"errors\":[{\"message\":\"denied\",\"extensions\":{\"code\":\"UNAUTHENTICATED\"}}]}"));
        ApiException error =
                assertThrows(ApiException.class, () -> client.createApplication(10L, 20L));
        assertTrue(error.getMessage().contains("UNAUTHENTICATED"));
        next();
        assertTrue(calls.isEmpty());
    }

    @Test
    void disconnectDoesNotReplayMutation() throws Exception {
        replies.add(new Reply(0, ""));
        assertThrows(ApiException.class, () -> client.createApplication(10L, 20L));
        next();
        assertTrue(calls.isEmpty());
    }

    @Test
    void missingResultAndNonJsonResponseAreErrors() {
        success("null");
        assertThrows(ApiException.class, () -> client.getApplicant(1L));
        replies.add(new Reply(502, "<html>Bad gateway</html>"));
        assertThrows(ApiException.class, () -> client.getApplicant(1L));
    }

    @Test
    void guestCannotSendProtectedMutation() {
        client.setSession(client.getSession().logout());
        assertThrows(ApiException.class, () -> client.deleteResume(20L));
        assertTrue(calls.isEmpty());
    }

    @Test
    void rejectedRefreshDoesNotReplayProtectedRequestOrReplaceSession() throws Exception {
        replies.add(new Reply(401, "{}"));
        replies.add(new Reply(401, "{}"));
        assertThrows(ApiException.class, () -> client.getApplicant(1L));
        assertEquals("GET_APPLICANT", next().body().path("operationName").asText());
        assertEquals("REFRESH", next().body().path("operationName").asText());
        assertTrue(calls.isEmpty());
        assertEquals("access", client.getSession().getAccessToken());
        assertEquals("refresh", client.getSession().getRefreshToken());
    }

    @Test
    void second401StopsAfterOneRefresh() throws Exception {
        replies.add(new Reply(401, "{}"));
        success("{\"accessToken\":\"new-access\",\"refreshToken\":\"new-refresh\"}");
        replies.add(new Reply(401, "{}"));
        assertThrows(ApiException.class, () -> client.getApplicant(1L));
        next();
        assertEquals("REFRESH", next().body().path("operationName").asText());
        assertEquals("Bearer new-access", next().auth());
        assertTrue(calls.isEmpty());
    }

    @Test
    void incompleteRefreshPairDoesNotOverwriteValidSession() throws Exception {
        success("{\"accessToken\":\"new-access\"}");
        assertThrows(ApiException.class, () -> client.refreshTokens());
        assertEquals("REFRESH", next().body().path("operationName").asText());
        assertEquals("access", client.getSession().getAccessToken());
        assertEquals("refresh", client.getSession().getRefreshToken());
        assertTrue(calls.isEmpty());
    }

    @Test
    void partialGraphqlDataWithErrorsCannotBeMistakenForSuccess() throws Exception {
        replies.add(
                new Reply(
                        200,
                        "{\"data\":{\"result\":{\"id\":\"1\"}},\"errors\":[{\"message\":\"denied\",\"extensions\":{\"code\":\"FORBIDDEN\"}}]}"));
        assertThrows(ApiException.class, () -> client.getApplicant(1L));
        next();
        assertTrue(calls.isEmpty());
    }
}
