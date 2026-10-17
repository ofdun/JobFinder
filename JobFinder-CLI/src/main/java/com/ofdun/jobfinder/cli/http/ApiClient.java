package com.ofdun.jobfinder.cli.http;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.ofdun.jobfinder.cli.auth.Session;
import com.ofdun.jobfinder.cli.auth.UserRole;
import com.ofdun.jobfinder.cli.dto.applications.*;
import com.ofdun.jobfinder.cli.dto.auth.TokenPair;
import com.ofdun.jobfinder.cli.dto.matching.MatchResult;
import com.ofdun.jobfinder.cli.dto.profiles.*;
import com.ofdun.jobfinder.cli.dto.resumes.*;
import com.ofdun.jobfinder.cli.dto.vacancies.*;
import com.ofdun.jobfinder.cli.logging.StructuredCliLogger;
import java.io.IOException;
import java.math.BigDecimal;
import java.time.Duration;
import java.util.HashMap;
import java.util.Map;
import lombok.Getter;
import lombok.NonNull;
import lombok.Setter;
import okhttp3.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class ApiClient {
    private static final MediaType JSON = MediaType.get("application/json; charset=utf-8");
    private static final Logger log = LoggerFactory.getLogger(ApiClient.class);
    private final OkHttpClient http =
            new OkHttpClient.Builder()
                    .callTimeout(Duration.ofSeconds(30))
                    .retryOnConnectionFailure(false)
                    .build();
    private final ObjectMapper mapper = Json.mapper();
    @Setter @Getter private Session session;

    public ApiClient(@NonNull Session session) {
        this.session = session;
    }

    public TokenPair login(UserRole role, String email, String password) {
        return execute(
                GraphqlOperation.LOGIN,
                Map.of(
                        "input",
                        Map.of("accountType", role.name(), "email", email, "password", password)),
                TokenPair.class);
    }

    public TokenPair refreshTokens() {
        if (session.getRole() == UserRole.GUEST || session.getRefreshToken() == null) {
            throw new ApiException("Требуется refresh-токен");
        }
        TokenPair pair =
                execute(
                        GraphqlOperation.REFRESH,
                        Map.of(
                                "input",
                                Map.of(
                                        "accountType",
                                        session.getRole().name(),
                                        "refreshToken",
                                        session.getRefreshToken())),
                        TokenPair.class);
        if (pair.accessToken() == null || pair.refreshToken() == null)
            throw new ApiException("Некорректная пара токенов");
        session = session.authorized(session.getRole(), pair.accessToken(), pair.refreshToken());
        return pair;
    }

    public Long createApplicant(ApplicantRequest input) {
        return createdId(GraphqlOperation.CREATE_APPLICANT, input);
    }

    public Applicant getApplicant(Long id) {
        return execute(GraphqlOperation.GET_APPLICANT, Map.of("id", id), Applicant.class);
    }

    public Applicant replaceApplicant(Long id, ApplicantRequest input) {
        return execute(
                GraphqlOperation.REPLACE_APPLICANT,
                Map.of("id", id, "input", input),
                Applicant.class);
    }

    public void deleteApplicant(Long id) {
        execute(GraphqlOperation.DELETE_APPLICANT, Map.of("id", id), Void.class);
    }

    public Long createEmployer(EmployerRequest input) {
        return createdId(GraphqlOperation.CREATE_EMPLOYER, input);
    }

    public Employer getEmployer(Long id) {
        return execute(GraphqlOperation.GET_EMPLOYER, Map.of("id", id), Employer.class);
    }

    public Employer replaceEmployer(Long id, EmployerRequest input) {
        return execute(
                GraphqlOperation.REPLACE_EMPLOYER,
                Map.of("id", id, "input", input),
                Employer.class);
    }

    public void deleteEmployer(Long id) {
        execute(GraphqlOperation.DELETE_EMPLOYER, Map.of("id", id), Void.class);
    }

    public Long createVacancy(VacancyRequest input) {
        return createdId(GraphqlOperation.CREATE_VACANCY, input);
    }

    public Vacancy getVacancy(Long id) {
        return execute(GraphqlOperation.GET_VACANCY, Map.of("id", id), Vacancy.class);
    }

    public Vacancy replaceVacancy(Long id, VacancyRequest input) {
        return execute(
                GraphqlOperation.REPLACE_VACANCY, Map.of("id", id, "input", input), Vacancy.class);
    }

    public void deleteVacancy(Long id) {
        execute(GraphqlOperation.DELETE_VACANCY, Map.of("id", id), Void.class);
    }

    public Long createResume(ResumeRequest input) {
        return createdId(GraphqlOperation.CREATE_RESUME, input);
    }

    public Resume getResume(Long id) {
        return execute(GraphqlOperation.GET_RESUME, Map.of("id", id), Resume.class);
    }

    public Resume replaceResume(Long id, ResumeUpdateRequest input) {
        return execute(
                GraphqlOperation.REPLACE_RESUME, Map.of("id", id, "input", input), Resume.class);
    }

    public void deleteResume(Long id) {
        execute(GraphqlOperation.DELETE_RESUME, Map.of("id", id), Void.class);
    }

    public Long createApplication(Long vacancyId, Long resumeId) {
        return createdId(
                GraphqlOperation.CREATE_APPLICATION,
                Map.of("vacancyId", vacancyId, "resumeId", resumeId));
    }

    public Application getApplication(Long id) {
        return execute(GraphqlOperation.GET_APPLICATION, Map.of("id", id), Application.class);
    }

    public Application updateApplicationStatus(Long id, ApplicationStatus status) {
        return execute(
                GraphqlOperation.UPDATE_APPLICATION_STATUS,
                Map.of("id", id, "status", status),
                Application.class);
    }

    public void deleteApplication(Long id) {
        execute(GraphqlOperation.DELETE_APPLICATION, Map.of("id", id), Void.class);
    }

    public Application[] myApplications(int limit, int offset) {
        return execute(
                GraphqlOperation.MY_APPLICATIONS,
                Map.of("page", Map.of("limit", limit, "offset", offset)),
                Application[].class);
    }

    public Application[] vacancyApplications(Long vacancyId, int limit, int offset) {
        return execute(
                GraphqlOperation.VACANCY_APPLICATIONS,
                Map.of("vacancyId", vacancyId, "page", Map.of("limit", limit, "offset", offset)),
                Application[].class);
    }

    public MatchResult[] matchingCandidates(Long vacancyId, Integer limit) {
        return execute(
                GraphqlOperation.MATCHING,
                Map.of("vacancyId", vacancyId, "limit", limit == null ? 10 : limit),
                MatchResult[].class);
    }

    public VacancyPageResponse searchVacancies(Map<String, Object> filters) {
        Map<String, Object> variables = searchVariables(filters);
        Map<String, Object> filter = new HashMap<>(filters);
        Object sort = filter.remove("sortBy");
        Object descending = filter.remove("sortDesc");
        filter.remove("limit");
        filter.remove("offset");
        variables.put("filter", filter);
        variables.put("sort", sort == null ? "PUBLICATION_DATE" : sort);
        variables.put("direction", Boolean.TRUE.equals(descending) ? "DESC" : "ASC");
        return execute(GraphqlOperation.SEARCH_VACANCIES, variables, VacancyPageResponse.class);
    }

    public ResumePageResponse searchResumes(Map<String, Object> filters) {
        return execute(
                GraphqlOperation.SEARCH_RESUMES,
                searchVariables(filters),
                ResumePageResponse.class);
    }

    private Map<String, Object> searchVariables(Map<String, Object> values) {
        Map<String, Object> filter = new HashMap<>(values);
        Object limit = filter.remove("limit"), offset = filter.remove("offset");
        Map<String, Object> variables = new HashMap<>();
        variables.put("filter", filter);
        variables.put(
                "page",
                Map.of("limit", limit == null ? 20 : limit, "offset", offset == null ? 0 : offset));
        return variables;
    }

    private Long createdId(GraphqlOperation operation, Object input) {
        JsonNode result = execute(operation, Map.of("input", input), JsonNode.class);
        try {
            return Long.valueOf(result.path("id").asText());
        } catch (NumberFormatException error) {
            throw new ApiException("Сервер вернул некорректный ID");
        }
    }

    public <T> T execute(
            GraphqlOperation operation, Map<String, Object> variables, Class<T> responseType) {
        if (operation.authenticationRequired() && !session.isAuthorized())
            throw new ApiException("Требуется авторизация");
        final Request request;
        try {
            HttpUrl url = HttpUrl.parse(session.getBaseUrl());
            if (url == null) throw new ApiException("Некорректный адрес API");
            JsonNode inputs = normalize(mapper.valueToTree(variables), "");
            String body =
                    mapper.writeValueAsString(
                            Map.of(
                                    "query",
                                    operation.document(),
                                    "operationName",
                                    operation.name(),
                                    "variables",
                                    inputs));
            Request.Builder builder =
                    new Request.Builder()
                            .url(url)
                            .header("Accept", "application/graphql-response+json")
                            .post(RequestBody.create(body, JSON));
            if (operation.authenticationRequired())
                builder.header("Authorization", "Bearer " + session.getAccessToken());
            request = builder.build();
        } catch (IOException | IllegalArgumentException error) {
            throw new ApiException("Не удалось подготовить запрос GraphQL: " + error.getMessage());
        }
        return send(request, responseType, operation.authenticationRequired());
    }

    // ID and Decimal are strings on the wire, while menu/domain DTOs keep their Java types.
    private JsonNode normalize(JsonNode value, String field) {
        if (value.isObject()) {
            ObjectNode object = (ObjectNode) value;
            var keys = new java.util.ArrayList<String>();
            object.fieldNames().forEachRemaining(keys::add);
            for (String key : keys) object.set(key, normalize(object.get(key), key));
        } else if (value.isArray()) {
            ArrayNode array = (ArrayNode) value;
            for (int i = 0; i < array.size(); i++) array.set(i, normalize(array.get(i), field));
        } else if (value.isNumber()
                && (field.equals("id") || field.endsWith("Id") || field.endsWith("Ids"))) {
            return mapper.getNodeFactory().textNode(value.asText());
        } else if (value.isNumber()
                && (field.equals("salary")
                        || field.equals("salaryMin")
                        || field.equals("salaryMax"))) {
            return mapper.getNodeFactory().textNode(new BigDecimal(value.asText()).toPlainString());
        }
        return value;
    }

    private <T> T send(Request request, Class<T> responseType, boolean mayRefresh) {
        long started = System.nanoTime();
        Integer status = null;
        String requestId = null;
        try (Response response = http.newCall(request).execute()) {
            status = response.code();
            requestId = response.header("X-Request-Id");
            if (status == 401 && mayRefresh && session.getRefreshToken() != null) {
                // Only a transport 401 is guaranteed to precede execution. Never replay a GraphQL
                // error.
                response.close();
                refreshTokens();
                return send(
                        request.newBuilder()
                                .header("Authorization", "Bearer " + session.getAccessToken())
                                .build(),
                        responseType,
                        false);
            }
            String body = response.body() == null ? "" : response.body().string();
            JsonNode envelope;
            try {
                envelope = mapper.readTree(body);
            } catch (IOException error) {
                throw new ApiException("HTTP " + status + ": сервер вернул не JSON");
            }
            if (envelope != null
                    && envelope.path("errors").isArray()
                    && !envelope.path("errors").isEmpty()) {
                StringBuilder message = new StringBuilder();
                for (JsonNode error : envelope.path("errors")) {
                    if (!message.isEmpty()) message.append("; ");
                    message.append(error.path("extensions").path("code").asText("GRAPHQL_ERROR"))
                            .append(": ")
                            .append(error.path("message").asText("Ошибка API"));
                }
                throw new ApiException(message.toString());
            }
            if (!response.isSuccessful()) throw new ApiException("HTTP " + status);
            JsonNode result = envelope == null ? null : envelope.path("data").get("result");
            if (result == null || result.isNull())
                throw new ApiException("Сервер не вернул результат операции");
            T decoded =
                    responseType == Void.class ? null : mapper.treeToValue(result, responseType);
            StructuredCliLogger.logHttpCall(
                    log,
                    session,
                    "POST",
                    request.url().encodedPath(),
                    requestId,
                    status,
                    (System.nanoTime() - started) / 1_000_000,
                    "success",
                    null);
            return decoded;
        } catch (IOException error) {
            StructuredCliLogger.logHttpCall(
                    log,
                    session,
                    "POST",
                    request.url().encodedPath(),
                    requestId,
                    status,
                    (System.nanoTime() - started) / 1_000_000,
                    "failure",
                    error);
            throw new ApiException("Ошибка сети или формата ответа: " + error.getMessage());
        } catch (ApiException error) {
            StructuredCliLogger.logHttpCall(
                    log,
                    session,
                    "POST",
                    request.url().encodedPath(),
                    requestId,
                    status,
                    (System.nanoTime() - started) / 1_000_000,
                    "failure",
                    null);
            throw error;
        }
    }
}
