package com.ofdun.jobfinder.cli.logging;

import com.ofdun.jobfinder.cli.auth.Session;
import java.util.function.Consumer;
import org.slf4j.Logger;
import org.slf4j.MDC;

public final class StructuredCliLogger {
    private StructuredCliLogger() {}

    public static void logUserAction(
            Logger logger, Session session, String action, Consumer<Logger> body) {
        putSessionContext(session);
        MDC.put("service", "JobFinder-CLI");
        MDC.put("event.action", action);
        try {
            body.accept(logger);
        } finally {
            clearActionContext();
        }
    }

    public static void logHttpCall(
            Logger logger,
            Session session,
            String method,
            String path,
            String requestId,
            Integer status,
            long durationMs,
            String outcome,
            Throwable error) {
        putSessionContext(session);
        MDC.put("service", "JobFinder-CLI");
        MDC.put("event.action", "cli.http.request");
        MDC.put("http.method", method);
        MDC.put("http.path", path);
        if (requestId != null && !requestId.isBlank()) {
            MDC.put("request_id", requestId);
        }
        if (status != null) {
            MDC.put("http.status", String.valueOf(status));
        }
        MDC.put("duration_ms", String.valueOf(durationMs));
        MDC.put("event.outcome", outcome);
        if (error != null) {
            MDC.put("error.type", error.getClass().getSimpleName());
        }

        try {
            if (error != null || (status != null && status >= 500)) {
                logger.error("CLI HTTP call finished", error);
            } else if (status != null && status >= 400) {
                logger.warn("CLI HTTP call finished");
            } else {
                logger.info("CLI HTTP call finished");
            }
        } finally {
            clearActionContext();
        }
    }

    private static void putSessionContext(Session session) {
        if (session == null) {
            MDC.put("actor.role", "guest");
            MDC.put("actor.id", "anonymous");
            return;
        }
        MDC.put("actor.role", session.getRole().name().toLowerCase());
        MDC.put("actor.id", session.isAuthorized() ? "authorized" : "guest");
    }

    private static void clearActionContext() {
        MDC.remove("service");
        MDC.remove("event.action");
        MDC.remove("http.method");
        MDC.remove("http.path");
        MDC.remove("request_id");
        MDC.remove("http.status");
        MDC.remove("duration_ms");
        MDC.remove("event.outcome");
        MDC.remove("error.type");
        MDC.remove("actor.role");
        MDC.remove("actor.id");
    }
}
