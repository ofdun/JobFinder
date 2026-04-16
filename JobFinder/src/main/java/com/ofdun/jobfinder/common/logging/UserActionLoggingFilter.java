package com.ofdun.jobfinder.common.logging;

import com.ofdun.jobfinder.features.auth.security.JwtAuthenticationFilter;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Optional;
import java.util.UUID;
import lombok.NonNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

@Component
public class UserActionLoggingFilter extends OncePerRequestFilter {
    private static final Logger log = LoggerFactory.getLogger(UserActionLoggingFilter.class);

    @Override
    protected void doFilterInternal(
            @NonNull HttpServletRequest request,
            @NonNull HttpServletResponse response,
            @NonNull FilterChain filterChain)
            throws ServletException, IOException {
        if (!request.getRequestURI().startsWith("/api/v1")) {
            filterChain.doFilter(request, response);
            return;
        }

        long startedAt = System.nanoTime();
        String requestId =
                Optional.ofNullable(request.getHeader("X-Request-Id"))
                        .filter(v -> !v.isBlank())
                        .orElseGet(() -> UUID.randomUUID().toString());

        MDC.put("service", "JobFinder");
        MDC.put("request_id", requestId);
        MDC.put("trace_id", requestId);
        response.setHeader("X-Request-Id", requestId);
        MDC.put("http.method", request.getMethod());
        MDC.put("http.path", request.getRequestURI());
        MDC.put("event.action", request.getMethod() + " " + request.getRequestURI());
        putActor(request);

        try {
            filterChain.doFilter(request, response);
            logOutcome(response.getStatus(), startedAt, null);
        } catch (Exception ex) {
            MDC.put("error.type", ex.getClass().getSimpleName());
            logOutcome(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, startedAt, ex);
            throw ex;
        } finally {
            MDC.clear();
        }
    }

    private void putActor(HttpServletRequest request) {
        Object actorId = request.getAttribute(JwtAuthenticationFilter.ATTR_ACTOR_ID);
        Object actorRole = request.getAttribute(JwtAuthenticationFilter.ATTR_ACTOR_ROLE);

        MDC.put("actor.id", actorId == null ? "anonymous" : String.valueOf(actorId));
        MDC.put("actor.role", actorRole == null ? "guest" : String.valueOf(actorRole));
    }

    private void logOutcome(int statusCode, long startedAt, Exception ex) {
        long durationMs = (System.nanoTime() - startedAt) / 1_000_000;
        String outcome = statusCode < 400 ? "success" : "failure";

        MDC.put("http.status", String.valueOf(statusCode));
        MDC.put("event.outcome", outcome);
        MDC.put("duration_ms", String.valueOf(durationMs));

        if (ex != null || statusCode >= 500) {
            log.error("User action finished", ex);
            return;
        }
        if (statusCode >= 400) {
            log.warn("User action finished");
            return;
        }
        log.info("User action finished");
    }
}


