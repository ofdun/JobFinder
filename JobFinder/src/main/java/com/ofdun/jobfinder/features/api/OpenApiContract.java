package com.ofdun.jobfinder.features.api;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import com.networknt.schema.*;
import jakarta.servlet.http.*;
import java.io.IOException;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.HandlerMapping;

@Component
public class OpenApiContract implements HandlerInterceptor {
    private final JsonNode document;
    private final ObjectMapper json = new ObjectMapper();
    private final SchemaRegistry registry =
            SchemaRegistry.withDefaultDialect(SpecificationVersion.DRAFT_2020_12);
    private final Map<String, Schema> schemas = new ConcurrentHashMap<>();

    public OpenApiContract() throws IOException {
        document =
                new ObjectMapper(new YAMLFactory())
                        .readTree(
                                new ClassPathResource("static/api/v2/openapi.yml")
                                        .getInputStream());
    }

    public JsonNode operation(HttpServletRequest request) {
        Object pattern = request.getAttribute(HandlerMapping.BEST_MATCHING_PATTERN_ATTRIBUTE);
        return pattern == null
                ? json.missingNode()
                : document.path("paths")
                        .path(pattern.toString())
                        .path(request.getMethod().toLowerCase(Locale.ROOT));
    }

    public void validateBody(HttpServletRequest request, byte[] body) throws IOException {
        JsonNode schema =
                operation(request)
                        .path("requestBody")
                        .path("content")
                        .path("application/json")
                        .path("schema");
        if (!schema.isMissingNode()) validate(schema, json.readTree(body));
    }

    public void validate(JsonNode schema, JsonNode value) {
        if (value == null) throw new IllegalArgumentException("Request body is required");
        Schema compiled =
                schemas.computeIfAbsent(
                        schema.toString(),
                        key -> {
                            ObjectNode root = json.createObjectNode();
                            root.set("components", document.path("components"));
                            root.set("allOf", json.createArrayNode().add(schema));
                            return registry.getSchema(root);
                        });
        var errors =
                compiled.validate(
                        value,
                        context ->
                                context.executionConfig(
                                        config -> config.formatAssertionsEnabled(true)));
        if (!errors.isEmpty())
            throw new IllegalArgumentException("Request does not match the API contract");
    }

    @Override
    public boolean preHandle(
            HttpServletRequest request, HttpServletResponse response, Object handler) {
        if (!request.getRequestURI().startsWith("/api/v2/")) return true;
        JsonNode op = operation(request);
        @SuppressWarnings("unchecked")
        Map<String, String> path =
                (Map<String, String>)
                        request.getAttribute(HandlerMapping.URI_TEMPLATE_VARIABLES_ATTRIBUTE);
        for (JsonNode parameter : op.path("parameters")) {
            JsonNode param =
                    parameter.has("$ref")
                            ? document.at(parameter.get("$ref").asText().substring(1))
                            : parameter;
            String name = param.path("name").asText();
            String raw =
                    param.path("in").asText().equals("path")
                            ? (path == null ? null : path.get(name))
                            : request.getParameter(name);
            if (raw == null) {
                if (param.path("required").asBoolean())
                    throw new IllegalArgumentException("Missing parameter");
                continue;
            }
            JsonNode schema = param.path("schema");
            try {
                JsonNode value =
                        switch (schema.path("type").asText()) {
                            case "integer" -> json.getNodeFactory().numberNode(Long.parseLong(raw));
                            case "number" ->
                                    json.getNodeFactory().numberNode(new java.math.BigDecimal(raw));
                            case "boolean" -> {
                                if (!raw.equals("true") && !raw.equals("false"))
                                    throw new IllegalArgumentException();
                                yield json.getNodeFactory().booleanNode(Boolean.parseBoolean(raw));
                            }
                            case "array" -> {
                                var array = json.createArrayNode();
                                for (String item : raw.split(",")) array.add(Long.parseLong(item));
                                yield array;
                            }
                            default -> json.getNodeFactory().textNode(raw);
                        };
                validate(schema, value);
            } catch (RuntimeException ex) {
                throw new IllegalArgumentException("Invalid parameter: " + name);
            }
        }
        String sort = request.getParameter("sortBy");
        if (sort != null
                && !(request.getRequestURI().contains("/vacancies")
                                ? List.of("id", "publicationDate", "salary")
                                : List.of("id", "date", "creationDate"))
                        .contains(sort))
            throw new IllegalArgumentException("Unsupported sort field");
        return true;
    }
}
