package com.ofdun.jobfinder.cli.http;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.ofdun.jobfinder.cli.auth.Session;
import com.ofdun.jobfinder.cli.dto.auth.RefreshRequest;
import com.ofdun.jobfinder.cli.dto.auth.TokenPair;
import java.io.IOException;
import java.time.Duration;
import java.util.Map;
import lombok.Getter;
import lombok.NonNull;
import lombok.Setter;
import okhttp3.HttpUrl;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public final class ApiClient {
    private static final MediaType JSON = MediaType.get("application/json; charset=utf-8");

    private final OkHttpClient http =
            new OkHttpClient.Builder().callTimeout(Duration.ofSeconds(30)).build();
    ;
    private final ObjectMapper mapper = Json.mapper();

    @Setter @Getter private Session session;

    public ApiClient(@NonNull Session session) {
        this.session = session;
    }

    public <T> T get(
            String path, Map<String, Object> query, Class<T> responseType, boolean authRequired) {
        HttpUrl url = buildUrl(path, query);
        Request.Builder b = new Request.Builder().url(url).get();
        addAuthIfNeeded(b, authRequired);
        return execute(b.build(), responseType, authRequired);
    }

    public <T> T post(String path, Object body, Class<T> responseType, boolean authRequired) {
        HttpUrl url = buildUrl(path, Map.of());
        RequestBody rb = jsonBody(body);
        Request.Builder b = new Request.Builder().url(url).post(rb);
        addAuthIfNeeded(b, authRequired);
        return execute(b.build(), responseType, authRequired);
    }

    public <T> T put(String path, Object body, Class<T> responseType, boolean authRequired) {
        HttpUrl url = buildUrl(path, Map.of());
        RequestBody rb = jsonBody(body);
        Request.Builder b = new Request.Builder().url(url).put(rb);
        addAuthIfNeeded(b, authRequired);
        return execute(b.build(), responseType, authRequired);
    }

    public void delete(String path, boolean authRequired) {
        HttpUrl url = buildUrl(path, Map.of());
        Request.Builder b = new Request.Builder().url(url).delete();
        addAuthIfNeeded(b, authRequired);
        execute(b.build(), Void.class, authRequired);
    }

    private HttpUrl buildUrl(String path, Map<String, Object> query) {
        HttpUrl base = HttpUrl.parse(session.getBaseUrl());
        if (base == null) {
            throw new IllegalStateException("Некорректный baseUrl: " + session.getBaseUrl());
        }
        HttpUrl.Builder b = base.newBuilder();
        String normalized = path.startsWith("/") ? path.substring(1) : path;
        for (String seg : normalized.split("/")) {
            if (!seg.isBlank()) b.addPathSegment(seg);
        }
        for (var e : query.entrySet()) {
            if (e.getValue() == null) continue;
            Object v = e.getValue();
            if (v instanceof Iterable<?> it) {
                for (Object item : it) {
                    if (item != null) b.addQueryParameter(e.getKey(), String.valueOf(item));
                }
            } else {
                b.addQueryParameter(e.getKey(), String.valueOf(v));
            }
        }
        return b.build();
    }

    private RequestBody jsonBody(Object body) {
        try {
            String json = body == null ? "{}" : mapper.writeValueAsString(body);
            return RequestBody.create(json, JSON);
        } catch (JsonProcessingException e) {
            throw new IllegalStateException("Не удалось сериализовать JSON", e);
        }
    }

    private void addAuthIfNeeded(Request.Builder b, boolean authRequired) {
        if (!authRequired) return;
        if (!session.isAuthorized()) {
            throw new ApiException("Требуется авторизация");
        }
        b.header("Authorization", "Bearer " + session.getAccessToken());
    }

    private <T> T execute(Request request, Class<T> responseType, boolean authRequired) {
        try (Response resp = http.newCall(request).execute()) {
            if (resp.code() == 401 && authRequired) {
                if (tryRefresh()) {
                    Request retry =
                            request.newBuilder()
                                    .header("Authorization", "Bearer " + session.getAccessToken())
                                    .build();
                    try (Response resp2 = http.newCall(retry).execute()) {
                        return handleResponse(resp2, responseType);
                    }
                }
            }
            return handleResponse(resp, responseType);
        } catch (IOException e) {
            throw new ApiException("Ошибка сети: " + e.getMessage());
        }
    }

    private <T> T handleResponse(Response resp, Class<T> responseType) throws IOException {
        String body = resp.body() == null ? "" : resp.body().string();
        if (resp.isSuccessful()) {
            if (responseType == Void.class) {
                return null;
            }
            if (body.isBlank()) {
                return null;
            }
            return mapper.readValue(body, responseType);
        }
        String msg = "HTTP " + resp.code();
        if (!body.isBlank()) msg += ": " + body;
        throw new ApiException(msg);
    }

    private boolean tryRefresh() {
        if (session.getRefreshToken() == null || session.getRefreshToken().isBlank()) {
            return false;
        }
        String refreshPath =
                switch (session.getRole()) {
                    case APPLICANT -> "/auth/applicant/refresh";
                    case EMPLOYER -> "/auth/employer/refresh";
                    default -> null;
                };
        if (refreshPath == null) return false;

        try {
            TokenPair pair =
                    post(
                            refreshPath,
                            new RefreshRequest(session.getRefreshToken()),
                            TokenPair.class,
                            false);
            if (pair == null || pair.accessToken() == null || pair.refreshToken() == null) {
                return false;
            }
            session =
                    session.authorized(session.getRole(), pair.accessToken(), pair.refreshToken());
            return true;
        } catch (ApiException ex) {
            return false;
        }
    }
}
