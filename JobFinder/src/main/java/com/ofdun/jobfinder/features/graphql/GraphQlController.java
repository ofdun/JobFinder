package com.ofdun.jobfinder.features.graphql;

import com.fasterxml.jackson.databind.ObjectMapper;
import graphql.ExecutionInput;
import graphql.GraphQL;
import jakarta.servlet.http.HttpServletRequest;
import java.util.*;
import lombok.RequiredArgsConstructor;
import org.springframework.http.*;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
public class GraphQlController {
    private final GraphQL graphQL;
    private final ObjectMapper json;
    private static final MediaType RESPONSE =
            MediaType.parseMediaType("application/graphql-response+json");

    @RequestMapping("/api/v2")
    public ResponseEntity<?> execute(HttpServletRequest request, Authentication authentication) {
        if (!request.getMethod().equals("POST"))
            return response(
                    405,
                    error("GRAPHQL_VALIDATION_FAILED", "Only POST is supported"),
                    "Allow",
                    "POST");
        try {
            if (request.getContentType() == null
                    || !MediaType.APPLICATION_JSON.isCompatibleWith(
                            MediaType.parseMediaType(request.getContentType())))
                return response(415, error("GRAPHQL_VALIDATION_FAILED", "Use application/json"));
            String accept = request.getHeader("Accept");
            if (accept != null
                    && MediaType.parseMediaTypes(accept).stream()
                            .noneMatch(
                                    t -> t.getQualityValue() > 0 && t.isCompatibleWith(RESPONSE)))
                return response(
                        406,
                        error(
                                "GRAPHQL_VALIDATION_FAILED",
                                "Use application/graphql-response+json"));
        } catch (InvalidMediaTypeException ex) {
            return response(400, error("GRAPHQL_VALIDATION_FAILED", "Invalid media type"));
        }
        if (request.getHeader("Authorization") != null
                && (authentication == null
                        || !(authentication.getPrincipal()
                                instanceof
                                com.ofdun.jobfinder.features.auth.security.JobFinderPrincipal)))
            return response(
                    401,
                    error("UNAUTHENTICATED", "Invalid bearer token"),
                    "WWW-Authenticate",
                    "Bearer");
        try {
            byte[] bytes = request.getInputStream().readNBytes(1048577);
            if (bytes.length > 1048576)
                return response(400, error("GRAPHQL_VALIDATION_FAILED", "Request too large"));
            var body = json.readTree(bytes);
            if (body == null
                    || !body.isObject()
                    || !body.path("query").isTextual()
                    || !body.path("operationName").isTextual()
                    || body.path("operationName").asText().isBlank()
                    || (body.has("variables")
                            && !body.get("variables").isNull()
                            && !body.get("variables").isObject()))
                return response(
                        400,
                        error(
                                "GRAPHQL_VALIDATION_FAILED",
                                "A named operation and variables object are required"));
            Map<String, Object> variables =
                    body.hasNonNull("variables")
                            ? json.convertValue(body.get("variables"), Map.class)
                            : Map.of();
            var input =
                    ExecutionInput.newExecutionInput()
                            .query(body.get("query").asText())
                            .operationName(body.get("operationName").asText())
                            .variables(variables);
            if (authentication != null)
                input.graphQLContext(Map.of("authentication", authentication));
            var result = graphQL.execute(input.build());
            var output = new LinkedHashMap<String, Object>();
            if (result.isDataPresent()) output.put("data", result.getData());
            if (!result.getErrors().isEmpty()) {
                var errors = new ArrayList<Map<String, Object>>();
                for (var e : result.getErrors()) {
                    var item = new LinkedHashMap<String, Object>();
                    String code =
                            result.isDataPresent()
                                    ? "INTERNAL_SERVER_ERROR"
                                    : (e.getErrorType() == graphql.ErrorType.InvalidSyntax
                                            ? "GRAPHQL_PARSE_FAILED"
                                            : "GRAPHQL_VALIDATION_FAILED");
                    if (e.getExtensions() != null && e.getExtensions().containsKey("code"))
                        code = String.valueOf(e.getExtensions().get("code"));
                    item.put(
                            "message",
                            result.isDataPresent() ? e.getMessage() : "Invalid GraphQL operation");
                    if (e.getPath() != null) item.put("path", e.getPath());
                    item.put("extensions", Map.of("code", code));
                    errors.add(item);
                }
                output.put("errors", errors);
            }
            return response(result.isDataPresent() ? 200 : 400, output);
        } catch (java.io.IOException ex) {
            return response(400, error("GRAPHQL_PARSE_FAILED", "Invalid JSON"));
        } catch (RuntimeException ex) {
            boolean unavailable = GraphQlConfiguration.errorCode(ex).equals("SERVICE_UNAVAILABLE");
            return response(
                    unavailable ? 503 : 500,
                    error(
                            unavailable ? "SERVICE_UNAVAILABLE" : "INTERNAL_SERVER_ERROR",
                            unavailable ? "Service unavailable" : "Internal server error"));
        }
    }

    private static Map<String, Object> error(String code, String message) {
        return Map.of(
                "errors", List.of(Map.of("message", message, "extensions", Map.of("code", code))));
    }

    private static ResponseEntity<?> response(int status, Object body, String... headers) {
        var builder =
                ResponseEntity.status(status)
                        .contentType(RESPONSE)
                        .cacheControl(CacheControl.noStore());
        for (int i = 0; i < headers.length; i += 2) builder.header(headers[i], headers[i + 1]);
        return builder.body(body);
    }
}
