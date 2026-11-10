package com.ofdun.jobfinder.common.logging;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

import jakarta.servlet.ServletException;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.slf4j.MDC;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

class UserActionLoggingFilterTest {
    private final UserActionLoggingFilter filter = new UserActionLoggingFilter();

    @AfterEach
    void clearContext() {
        MDC.clear();
    }

    @Test
    void requestIdDoesNotReplaceTraceContext() throws Exception {
        MDC.put("traceId", "existing-trace");
        MDC.put("spanId", "existing-span");
        var request = new MockHttpServletRequest("GET", "/api/v1/categories");
        request.addHeader("X-Request-Id", "request-id");
        var response = new MockHttpServletResponse();
        filter.doFilter(
                request,
                response,
                (req, res) -> {
                    assertEquals("existing-trace", MDC.get("traceId"));
                    assertEquals("existing-span", MDC.get("spanId"));
                    assertEquals("request-id", MDC.get("request_id"));
                    assertNull(MDC.get("trace_id"));
                });
        assertEquals("request-id", response.getHeader("X-Request-Id"));
        assertEquals("existing-trace", MDC.get("traceId"));
        assertNull(MDC.get("request_id"));
    }

    @Test
    void failureRestoresOuterContext() {
        MDC.put("traceId", "existing-trace");
        var request = new MockHttpServletRequest("GET", "/api/v1/categories");
        assertThrows(
                ServletException.class,
                () ->
                        filter.doFilter(
                                request,
                                new MockHttpServletResponse(),
                                (req, res) -> {
                                    throw new ServletException("failure");
                                }));
        assertEquals("existing-trace", MDC.get("traceId"));
        assertNull(MDC.get("error.type"));
        assertNull(MDC.get("http.path"));
    }
}
