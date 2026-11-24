package com.ofdun.jobfinder.common.observability;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import com.ofdun.jobfinder.features.api.ApiFailure;
import com.ofdun.jobfinder.features.auth.mfa.AuthenticationChallenge;
import com.ofdun.jobfinder.features.vacancy.data.postgres.entity.VacancyEntity;
import com.ofdun.jobfinder.features.vacancy.data.postgres.repository.PostgreSQLVacancyRepository;
import com.ofdun.jobfinder.features.vacancy.data.postgres.repository.VacancyJpaRepository;
import com.ofdun.jobfinder.features.vacancy.domain.repository.VacancyRepository;
import com.ofdun.jobfinder.features.vacancy.domain.service.BasicVacancyService;
import com.ofdun.jobfinder.features.vacancy.domain.service.VacancyService;
import com.ofdun.jobfinder.features.vacancy.domain.validator.VacancyValidator;
import io.micrometer.observation.Observation;
import io.micrometer.observation.ObservationHandler;
import io.micrometer.observation.ObservationRegistry;
import io.micrometer.observation.annotation.Observed;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.springframework.boot.autoconfigure.AutoConfigurations;
import org.springframework.boot.autoconfigure.aop.AopAutoConfiguration;
import org.springframework.boot.micrometer.observation.autoconfigure.ObservationAutoConfiguration;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.Ordered;

class TraceDiagnosticsTest {
    private final ApplicationContextRunner runner =
            new ApplicationContextRunner()
                    .withConfiguration(
                            AutoConfigurations.of(
                                    AopAutoConfiguration.class, ObservationAutoConfiguration.class))
                    .withPropertyValues(
                            "management.observations.annotations.enabled=true",
                            "management.observations.enable.spring.security=false")
                    .withUserConfiguration(ConfigurationForTest.class);

    @Test
    void httpSpanContainsVacancyIdWithoutAddingMetricLabels() {
        var request =
                new org.springframework.mock.web.MockHttpServletRequest(
                        "GET", "/api/v1/vacancies/42");
        var context =
                new org.springframework.http.server.observation.ServerRequestObservationContext(
                        request, new org.springframework.mock.web.MockHttpServletResponse());
        new TraceDiagnostics().map(context);
        assertEquals("42", context.getHighCardinalityKeyValue("vacancy.id").getValue());
        assertEquals(
                "/api/v1/vacancies/42", context.getHighCardinalityKeyValue("url.path").getValue());
        assertNull(context.getLowCardinalityKeyValue("vacancy.id"));
    }

    @Test
    void searchSpanDoesNotGetAVacancyIdOrQueryParameters() {
        var request =
                new org.springframework.mock.web.MockHttpServletRequest(
                        "GET", "/api/v1/vacancies/search");
        request.setQueryString("token=private");
        var context =
                new org.springframework.http.server.observation.ServerRequestObservationContext(
                        request, new org.springframework.mock.web.MockHttpServletResponse());
        new TraceDiagnostics().map(context);
        assertNull(context.getHighCardinalityKeyValue("vacancy.id"));
        assertEquals(
                "/api/v1/vacancies/search",
                context.getHighCardinalityKeyValue("url.path").getValue());
    }

    @Test
    void serviceAndRepositoryRemainChildrenOfTheRequest() {
        runner.run(
                context -> {
                    var entity = new VacancyEntity();
                    entity.setId(42L);
                    when(context.getBean(VacancyJpaRepository.class).findById(42L))
                            .thenReturn(Optional.of(entity));
                    var registry = context.getBean(ObservationRegistry.class);
                    var request = Observation.start("http.server.requests", registry);
                    try (var scope = request.openScope()) {
                        assertEquals(
                                42L,
                                context.getBean(VacancyService.class).getVacancyById(42L).getId());
                        assertSame(request, registry.getCurrentObservation());
                    } finally {
                        request.stop();
                    }
                    var recorded = context.getBean(RecordingHandler.class).stopped;
                    var repository = recorded.get(0);
                    var service = recorded.get(1);
                    assertEquals(
                            "PostgreSQL: получение вакансии по ID", repository.getContextualName());
                    assertEquals("Получение вакансии по ID", service.getContextualName());
                    assertSame(service, repository.getParentObservation().getContextView());
                    assertSame(
                            request.getContext(), service.getParentObservation().getContextView());
                    assertEquals(
                            "42", repository.getHighCardinalityKeyValue("entity.id").getValue());
                    assertEquals(
                            "repository", repository.getLowCardinalityKeyValue("layer").getValue());
                    assertEquals(
                            "PostgreSQL",
                            repository.getLowCardinalityKeyValue("storage").getValue());
                    assertNull(registry.getCurrentObservation());
                });
    }

    @Test
    void failureClosesScopesAndPreservesTheOriginalException() {
        runner.run(
                context -> {
                    var failure =
                            new IllegalStateException(
                                    "database unavailable", new java.net.ConnectException());
                    when(context.getBean(VacancyJpaRepository.class).findById(42L))
                            .thenThrow(failure);
                    assertSame(
                            failure,
                            assertThrows(
                                    IllegalStateException.class,
                                    () ->
                                            context.getBean(VacancyService.class)
                                                    .getVacancyById(42L)));
                    var stopped = context.getBean(RecordingHandler.class).stopped;
                    assertEquals(2, stopped.size());
                    for (var span : stopped) {
                        assertSame(failure, span.getError());
                        assertEquals(
                                "failure",
                                span.getLowCardinalityKeyValue("operation.outcome").getValue());
                        assertEquals(
                                "java.net.ConnectException",
                                span.getLowCardinalityKeyValue("error.root_type").getValue());
                    }
                    assertNull(context.getBean(ObservationRegistry.class).getCurrentObservation());
                });
    }

    @Test
    void businessCodeIsRecordedButCredentialsAreNot() {
        runner.run(
                context -> {
                    assertThrows(
                            ApiFailure.class,
                            () ->
                                    context.getBean(AuthenticationOperation.class)
                                            .fail(
                                                    7L,
                                                    "private-email",
                                                    "private-password",
                                                    "private-token"));
                    var span = context.getBean(RecordingHandler.class).stopped.getFirst();
                    assertEquals(
                            "ACCOUNT_LOCKED",
                            span.getLowCardinalityKeyValue("error.code").getValue());
                    assertEquals(
                            "423",
                            span.getLowCardinalityKeyValue("http.response.status_code").getValue());
                    assertEquals("7", span.getHighCardinalityKeyValue("entity.id").getValue());
                    assertFalse(span.getAllKeyValues().toString().contains("private-"));
                });
    }

    @Test
    void requestingTheSecondFactorIsNotAnError() {
        runner.run(
                context -> {
                    assertThrows(
                            AuthenticationChallenge.class,
                            () -> context.getBean(AuthenticationOperation.class).challenge());
                    var recorder = context.getBean(RecordingHandler.class);
                    var span = recorder.stopped.getFirst();
                    assertNull(span.getError());
                    assertEquals(
                            "challenge",
                            span.getLowCardinalityKeyValue("operation.outcome").getValue());
                    assertEquals(
                            "TOTP", span.getLowCardinalityKeyValue("auth.next_step").getValue());
                    assertEquals(0, recorder.errors.size());
                    assertFalse(span.getAllKeyValues().toString().contains("private-token"));
                });
    }

    @Test
    void securityInternalsAreDisabledWithoutDisablingHttp() {
        runner.run(
                context -> {
                    var registry = context.getBean(ObservationRegistry.class);
                    Observation.start("spring.security.http.chains", registry).stop();
                    Observation.start("http.server.requests", registry).stop();
                    var spans = context.getBean(RecordingHandler.class).stopped;
                    assertEquals(1, spans.size());
                    assertEquals("http.server.requests", spans.getFirst().getName());
                });
    }

    @Configuration(proxyBeanMethods = false)
    static class ConfigurationForTest {
        @Bean
        jakarta.persistence.EntityManagerFactory entityManagerFactory() {
            var factory = mock(jakarta.persistence.EntityManagerFactory.class);
            when(factory.getProperties()).thenReturn(java.util.Map.of());
            return factory;
        }

        @Bean
        TraceDiagnostics diagnostics() {
            return new TraceDiagnostics();
        }

        @Bean
        RecordingHandler recorder() {
            return new RecordingHandler();
        }

        @Bean
        VacancyJpaRepository storage() {
            return mock(VacancyJpaRepository.class);
        }

        @Bean
        VacancyRepository repository(VacancyJpaRepository storage) {
            return new PostgreSQLVacancyRepository(storage);
        }

        @Bean
        VacancyService service(VacancyRepository repository) {
            return new BasicVacancyService(repository, mock(VacancyValidator.class));
        }

        @Bean
        AuthenticationOperation authenticationOperation() {
            return new AuthenticationOperation();
        }
    }

    static class RecordingHandler implements ObservationHandler<Observation.Context>, Ordered {
        final List<Observation.Context> stopped = new ArrayList<>();
        final List<Throwable> errors = new ArrayList<>();

        @Override
        public int getOrder() {
            return Ordered.LOWEST_PRECEDENCE;
        }

        @Override
        public boolean supportsContext(Observation.Context context) {
            return true;
        }

        @Override
        public void onStop(Observation.Context context) {
            stopped.add(context);
        }

        @Override
        public void onError(Observation.Context context) {
            if (context.getError() != null) errors.add(context.getError());
        }
    }

    static class AuthenticationOperation {
        @Observed(name = "jobfinder.auth.fail", contextualName = "Вход в аккаунт")
        public void fail(Long id, String email, String password, String token) {
            throw new ApiFailure("ACCOUNT_LOCKED", 423, "Account temporarily locked");
        }

        @Observed(name = "jobfinder.auth.challenge", contextualName = "Вход в аккаунт")
        public void challenge() {
            throw new AuthenticationChallenge("private-token", "TOTP");
        }
    }
}
