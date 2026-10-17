package com.ofdun.jobfinder.features.graphql;

import graphql.GraphQLContext;
import graphql.execution.CoercedVariables;
import graphql.language.StringValue;
import graphql.language.Value;
import graphql.schema.*;
import java.math.BigDecimal;
import java.net.URI;
import java.time.*;
import java.util.List;
import java.util.Locale;
import java.util.function.Function;

public final class GraphQlScalars {
    private GraphQlScalars() {}

    public static List<GraphQLScalarType> all() {
        return List.of(
                scalar("Date", s -> LocalDate.parse(s).toString()),
                scalar(
                        "DateTime",
                        s -> {
                            if (!s.matches("\\d{4}-\\d{2}-\\d{2}T.*(Z|[+-]\\d{2}:\\d{2})"))
                                throw new IllegalArgumentException();
                            return OffsetDateTime.parse(s).toInstant().toString();
                        }),
                scalar(
                        "Email",
                        s -> {
                            if (!s.matches("[^\\s@]+@[^\\s@]+\\.[^\\s@]+"))
                                throw new IllegalArgumentException();
                            return s;
                        }),
                scalar(
                        "URL",
                        s -> {
                            var uri = URI.create(s);
                            if (!(uri.getScheme().equalsIgnoreCase("http")
                                            || uri.getScheme().equalsIgnoreCase("https"))
                                    || uri.getHost() == null) throw new IllegalArgumentException();
                            return s;
                        }),
                scalar(
                        "Decimal",
                        s -> {
                            if (!s.matches("(0|[1-9][0-9]*)(?:\\.[0-9]+)?"))
                                throw new IllegalArgumentException();
                            return new BigDecimal(s).toPlainString();
                        }),
                scalar(
                        "Long",
                        s -> {
                            if (!s.matches("(0|[1-9][0-9]*)") || Long.parseLong(s) < 0)
                                throw new IllegalArgumentException();
                            return Long.toString(Long.parseLong(s));
                        }));
    }

    private static GraphQLScalarType scalar(String name, Function<String, String> parse) {
        return GraphQLScalarType.newScalar()
                .name(name)
                .coercing(
                        new Coercing<String, String>() {
                            private String input(Object value) {
                                if (!(value instanceof String s))
                                    throw new IllegalArgumentException();
                                return parse.apply(s);
                            }

                            @Override
                            public String parseValue(
                                    Object value, GraphQLContext context, Locale locale) {
                                try {
                                    return input(value);
                                } catch (RuntimeException ex) {
                                    throw new CoercingParseValueException("Invalid " + name);
                                }
                            }

                            @Override
                            public String parseLiteral(
                                    Value<?> value,
                                    CoercedVariables variables,
                                    GraphQLContext context,
                                    Locale locale) {
                                try {
                                    if (!(value instanceof StringValue s))
                                        throw new IllegalArgumentException();
                                    return input(s.getValue());
                                } catch (RuntimeException ex) {
                                    throw new CoercingParseLiteralException("Invalid " + name);
                                }
                            }

                            @Override
                            public String serialize(
                                    Object value, GraphQLContext context, Locale locale) {
                                try {
                                    if (value instanceof java.util.Date d)
                                        return name.equals("Date")
                                                ? d.toInstant()
                                                        .atOffset(ZoneOffset.UTC)
                                                        .toLocalDate()
                                                        .toString()
                                                : d.toInstant().toString();
                                    if (value instanceof Number n && name.equals("Decimal"))
                                        return parse.apply(
                                                new BigDecimal(n.toString()).toPlainString());
                                    return parse.apply(String.valueOf(value));
                                } catch (RuntimeException ex) {
                                    throw new CoercingSerializeException("Invalid " + name);
                                }
                            }
                        })
                .build();
    }
}
