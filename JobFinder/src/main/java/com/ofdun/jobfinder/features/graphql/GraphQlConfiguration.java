package com.ofdun.jobfinder.features.graphql;

import com.ofdun.jobfinder.features.api.ApiFailure;
import graphql.GraphQL;
import graphql.GraphqlErrorBuilder;
import graphql.analysis.MaxQueryComplexityInstrumentation;
import graphql.analysis.MaxQueryDepthInstrumentation;
import graphql.execution.DataFetcherExceptionHandlerResult;
import graphql.execution.instrumentation.ChainedInstrumentation;
import graphql.schema.idl.*;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ClassPathResource;

@Configuration
public class GraphQlConfiguration {
    @Bean
    public GraphQL graphQL(GraphQlOperations operations) throws IOException {
        String sdl =
                new ClassPathResource("graphql/schema.graphqls")
                        .getContentAsString(StandardCharsets.UTF_8);
        var types = new SchemaParser().parse(sdl);
        var wiring = RuntimeWiring.newRuntimeWiring();
        GraphQlScalars.all().forEach(wiring::scalar);
        for (String root : List.of("Query", "Mutation")) {
            var type = TypeRuntimeWiring.newTypeWiring(root);
            var definition =
                    (graphql.language.ObjectTypeDefinition) types.getType(root).orElseThrow();
            definition
                    .getFieldDefinitions()
                    .forEach(
                            f ->
                                    type.dataFetcher(
                                            f.getName(),
                                            root.equals("Query")
                                                    ? operations::query
                                                    : operations::mutation));
            wiring.type(type);
        }
        wiring.type(
                TypeRuntimeWiring.newTypeWiring("Vacancy")
                        .dataFetcher("employer", operations::related));
        wiring.type(
                TypeRuntimeWiring.newTypeWiring("Application")
                        .dataFetcher("resume", operations::related)
                        .dataFetcher("vacancy", operations::related));
        wiring.type(
                TypeRuntimeWiring.newTypeWiring("MatchResult")
                        .dataFetcher("resume", operations::related));
        return GraphQL.newGraphQL(new SchemaGenerator().makeExecutableSchema(types, wiring.build()))
                .instrumentation(
                        new ChainedInstrumentation(
                                List.of(
                                        new MaxQueryDepthInstrumentation(8),
                                        new MaxQueryComplexityInstrumentation(200))))
                .defaultDataFetcherExceptionHandler(
                        parameters -> {
                            Throwable ex = parameters.getException();
                            while (ex.getCause() != null
                                    && (ex instanceof java.util.concurrent.CompletionException
                                            || ex
                                                    instanceof
                                                    java.lang.reflect.InvocationTargetException))
                                ex = ex.getCause();
                            String code = errorCode(ex);
                            if (code.equals("INTERNAL_SERVER_ERROR")
                                    || code.equals("SERVICE_UNAVAILABLE"))
                                org.slf4j.LoggerFactory.getLogger(GraphQlConfiguration.class)
                                        .error("GraphQL field {} failed", parameters.getPath(), ex);
                            String message =
                                    switch (code) {
                                        case "UNAUTHENTICATED" -> "Authentication required";
                                        case "FORBIDDEN" -> "Access denied";
                                        case "NOT_FOUND" -> "Resource not found";
                                        case "CONFLICT" -> "Resource conflict";
                                        case "BAD_USER_INPUT" -> "Invalid input";
                                        case "SERVICE_UNAVAILABLE" -> "Service unavailable";
                                        default -> "Internal server error";
                                    };
                            var error =
                                    GraphqlErrorBuilder.newError(
                                                    parameters.getDataFetchingEnvironment())
                                            .message(message)
                                            .extensions(Map.of("code", code))
                                            .build();
                            return CompletableFuture.completedFuture(
                                    DataFetcherExceptionHandlerResult.newResult(error).build());
                        })
                .build();
    }

    static String errorCode(Throwable ex) {
        if (ex instanceof ApiFailure f) return f.getCode();
        String name = ex.getClass().getSimpleName();
        if (name.contains("NotFound")) return "NOT_FOUND";
        if (name.contains("AlreadyExists")
                || ex instanceof org.springframework.dao.DataIntegrityViolationException)
            return "CONFLICT";
        if (name.equals("InvalidPasswordException")
                || name.equals("InvalidRefreshTokenException")
                || name.equals("SessionIsOverException")) return "UNAUTHENTICATED";
        if (ex instanceof org.springframework.security.access.AccessDeniedException)
            return "FORBIDDEN";
        if (ex instanceof IllegalArgumentException
                || ex instanceof jakarta.validation.ValidationException) return "BAD_USER_INPUT";
        if (ex instanceof org.springframework.dao.DataAccessResourceFailureException
                || name.contains("Connection")
                || name.contains("Timeout")) return "SERVICE_UNAVAILABLE";
        return "INTERNAL_SERVER_ERROR";
    }
}
