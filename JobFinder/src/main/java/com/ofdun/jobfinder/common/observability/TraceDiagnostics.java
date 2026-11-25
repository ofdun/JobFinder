package com.ofdun.jobfinder.common.observability;

import com.ofdun.jobfinder.features.api.ApiFailure;
import com.ofdun.jobfinder.features.auth.mfa.AuthenticationChallenge;
import graphql.schema.DataFetchingEnvironment;
import io.micrometer.common.KeyValue;
import io.micrometer.observation.Observation;
import io.micrometer.observation.ObservationFilter;
import io.micrometer.observation.ObservationHandler;
import io.micrometer.observation.aop.ObservedAspect.ObservedAspectContext;
import org.aspectj.lang.reflect.MethodSignature;
import org.slf4j.MDC;
import org.springframework.aop.support.AopUtils;
import org.springframework.core.Ordered;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import org.springframework.web.filter.ServerHttpObservationFilter;

@Component
public class TraceDiagnostics
        implements ObservationHandler<Observation.Context>, ObservationFilter, Ordered {
    @Override
    public int getOrder() {
        return Ordered.HIGHEST_PRECEDENCE;
    }

    @Override
    public boolean supportsContext(Observation.Context context) {
        return context instanceof ObservedAspectContext;
    }

    @Override
    public void onStart(Observation.Context context) {
        var invocation = ((ObservedAspectContext) context).getProceedingJoinPoint();
        var signature = (MethodSignature) invocation.getSignature();
        var method =
                AopUtils.getMostSpecificMethod(
                        signature.getMethod(), invocation.getTarget().getClass());
        context.addLowCardinalityKeyValue(
                KeyValue.of("code.namespace", method.getDeclaringClass().getName()));
        context.addLowCardinalityKeyValue(KeyValue.of("code.function", method.getName()));
        String requestId = MDC.get("request_id");
        if (requestId != null)
            context.addHighCardinalityKeyValue(KeyValue.of("request.id", requestId));
        var parameters = method.getParameters();
        var arguments = invocation.getArgs();
        for (int i = 0; i < parameters.length; i++) {
            String name = parameters[i].getName();
            if ((name.equals("id") || name.endsWith("Id")) && arguments[i] instanceof Number id) {
                context.addHighCardinalityKeyValue(KeyValue.of("entity." + name, id.toString()));
            }
            if (arguments[i] instanceof DataFetchingEnvironment environment) {
                context.addLowCardinalityKeyValue(
                        KeyValue.of("graphql.field", environment.getFieldDefinition().getName()));
            }
        }
    }

    @Override
    public void onError(Observation.Context context) {
        if (context.getError() instanceof AuthenticationChallenge challenge) {
            context.addLowCardinalityKeyValue(
                    KeyValue.of("auth.next_step", challenge.getNextStep()));
            context.addLowCardinalityKeyValue(KeyValue.of("operation.outcome", "challenge"));
            context.setError(null);
        }
    }

    @Override
    public Observation.Context map(Observation.Context context) {
        if (context
                instanceof
                org.springframework.http.server.observation.ServerRequestObservationContext http) {
            addHttpAttributes(http);
            return context;
        }
        if (!(context instanceof ObservedAspectContext)) return context;
        Throwable error = context.getError();
        if (context.getLowCardinalityKeyValue("operation.outcome") == null) {
            context.addLowCardinalityKeyValue(
                    KeyValue.of("operation.outcome", error == null ? "success" : "failure"));
        }
        if (error == null) return context;
        context.addLowCardinalityKeyValue(KeyValue.of("error.type", error.getClass().getName()));
        Throwable cause = error;
        for (int i = 0; i < 10 && cause.getCause() != null && cause.getCause() != cause; i++)
            cause = cause.getCause();
        context.addLowCardinalityKeyValue(
                KeyValue.of("error.root_type", cause.getClass().getName()));
        if (error instanceof ApiFailure failure) {
            context.addLowCardinalityKeyValue(KeyValue.of("error.code", failure.getCode()));
            context.addLowCardinalityKeyValue(
                    KeyValue.of(
                            "http.response.status_code", Integer.toString(failure.getStatus())));
        }
        return context;
    }

    private static void addHttpAttributes(
            org.springframework.http.server.observation.ServerRequestObservationContext context) {
        var request = context.getCarrier();
        if (request != null) {
            String path = request.getRequestURI();
            context.addHighCardinalityKeyValue(KeyValue.of("url.path", path));
            var match =
                    java.util.regex.Pattern.compile("/api/v[12]/vacancies/([0-9]+)(?:/.*)?")
                            .matcher(path);
            if (match.matches())
                context.addHighCardinalityKeyValue(KeyValue.of("vacancy.id", match.group(1)));
        }
    }

    public static void recordHttpFailure(Exception error, String code) {
        if (!(RequestContextHolder.getRequestAttributes()
                instanceof ServletRequestAttributes attributes)) return;
        ServerHttpObservationFilter.findObservationContext(attributes.getRequest())
                .ifPresent(
                        context -> {
                            context.setError(error);
                            context.addLowCardinalityKeyValue(
                                    KeyValue.of("error.type", error.getClass().getName()));
                            context.addLowCardinalityKeyValue(KeyValue.of("error.code", code));
                        });
    }
}
