package com.ofdun.jobfinder.features.auth.security;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import com.ofdun.jobfinder.common.logging.UserActionLoggingFilter;
import com.ofdun.jobfinder.features.auth.domain.jwt.JwtProvider;
import com.ofdun.jobfinder.features.auth.enums.AccountType;
import jakarta.servlet.FilterChain;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpHeaders;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.mock.web.MockServletContext;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.FilterChainProxy;
import org.springframework.web.context.support.AnnotationConfigWebApplicationContext;

@Tag("equivalence")
class SecurityConfigurationTest {
    private AnnotationConfigWebApplicationContext context;
    private FilterChainProxy security;
    private JwtProvider provider;

    @Configuration(proxyBeanMethods = false)
    @EnableWebSecurity
    static class Dependencies {
        @Bean
        JwtProvider jwtProvider() {
            return mock(JwtProvider.class);
        }

        @Bean
        UserActionLoggingFilter userActionLoggingFilter() {
            return new UserActionLoggingFilter();
        }
    }

    @BeforeEach
    void setUp() {
        SecurityContextHolder.clearContext();
        context = new AnnotationConfigWebApplicationContext();
        context.setServletContext(new MockServletContext());
        context.register(Dependencies.class, SecurityConfiguration.class);
        context.refresh();
        security = context.getBean("springSecurityFilterChain", FilterChainProxy.class);
        provider = context.getBean(JwtProvider.class);
    }

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
        if (context != null) {
            context.close();
        }
    }

    private MockHttpServletRequest request(String method, String path) {
        var request = new MockHttpServletRequest(context.getServletContext(), method, path);
        request.setServletPath(path);
        return request;
    }

    @ParameterizedTest
    @CsvSource({
        "OPTIONS,/api/v1/private",
        "GET,/error",
        "POST,/api/v1/auth/login",
        "POST,/api/v1/auth/refresh",
        "POST,/api/v1/applicants",
        "POST,/api/v1/employers",
        "GET,/api/v1/categories",
        "GET,/api/v1/skills",
        "GET,/api/v1/languages",
        "GET,/api/v1/locations",
        "GET,/api/v1/locations/1",
        "GET,/api/v1/vacancies/1",
        "GET,/api/v1/resumes/1"
    })
    void securityFilterChain_whenPublicRoute_allowsAnonymousRequest(String method, String path)
            throws Exception {
        var request = request(method, path);
        var response = new MockHttpServletResponse();
        var downstream = mock(FilterChain.class);

        security.doFilter(request, response, downstream);

        assertEquals(200, response.getStatus());
        verify(downstream).doFilter(any(), any());
        verifyNoInteractions(provider);
        assertNull(request.getSession(false));
    }

    @ParameterizedTest
    @CsvSource({
        "GET,/api/v1/applicants",
        "GET,/api/v1/employers",
        "POST,/api/v1/vacancies",
        "PUT,/api/v1/vacancies/1",
        "DELETE,/api/v1/resumes/1",
        "POST,/api/v1/categories",
        "GET,/api/v1/vacancies/1/private",
        "GET,/api/v1/applications",
        "GET,/private"
    })
    void securityFilterChain_whenProtectedRoute_rejectsAnonymousRequest(String method, String path)
            throws Exception {
        var request = request(method, path);
        var response = new MockHttpServletResponse();
        var downstream = mock(FilterChain.class);

        security.doFilter(request, response, downstream);

        assertEquals(403, response.getStatus());
        verifyNoInteractions(downstream, provider);
        assertNull(request.getSession(false));
    }

    @Test
    void securityFilterChain_whenTokenValid_allowsStatelessPostWithoutCsrfToken() throws Exception {
        var request = request("POST", "/api/v1/vacancies");
        request.addHeader(HttpHeaders.AUTHORIZATION, "Bearer signed-token");
        var response = new MockHttpServletResponse();
        when(provider.getAccountType("signed-token")).thenReturn(AccountType.EMPLOYER);
        when(provider.validateToken("signed-token", AccountType.EMPLOYER)).thenReturn(true);
        when(provider.getUserId("signed-token")).thenReturn(42L);
        var observed = new AtomicReference<Authentication>();
        FilterChain downstream =
                (req, res) -> {
                    observed.set(SecurityContextHolder.getContext().getAuthentication());
                    res.getWriter().write("created");
                };

        security.doFilter(request, response, downstream);

        assertEquals(200, response.getStatus());
        assertEquals("created", response.getContentAsString());
        assertNotNull(observed.get());
        assertEquals(
                new JobFinderPrincipal(42L, AccountType.EMPLOYER), observed.get().getPrincipal());
        assertNull(request.getSession(false));
        assertNull(SecurityContextHolder.getContext().getAuthentication());
    }

    @Test
    void securityFilterChain_whenTokenInvalid_rejectsProtectedRequest() throws Exception {
        var request = request("GET", "/api/v1/applications");
        request.addHeader(HttpHeaders.AUTHORIZATION, "Bearer invalid-token");
        var response = new MockHttpServletResponse();
        var downstream = mock(FilterChain.class);
        when(provider.getAccountType("invalid-token")).thenReturn(AccountType.APPLICANT);
        when(provider.validateToken("invalid-token", AccountType.APPLICANT)).thenReturn(false);

        security.doFilter(request, response, downstream);

        assertEquals(403, response.getStatus());
        verifyNoInteractions(downstream);
        assertNull(SecurityContextHolder.getContext().getAuthentication());
    }

    @Test
    void securityFilterChain_placesLoggingAfterAuthentication() {
        var filters = security.getFilterChains().getFirst().getFilters();

        var types = filters.stream().map(Object::getClass).toList();

        assertTrue(types.contains(JwtAuthenticationFilter.class));
        assertTrue(types.contains(UserActionLoggingFilter.class));
        assertTrue(
                types.indexOf(JwtAuthenticationFilter.class)
                        < types.indexOf(UserActionLoggingFilter.class));
    }

    @Test
    void corsConfigurationSource_whenApiPath_setsAllowedOriginsMethodsAndHeaders() {
        var source = new SecurityConfiguration().corsConfigurationSource();
        var request = request("GET", "/api/v1/vacancies/1");

        var configuration = source.getCorsConfiguration(request);

        assertNotNull(configuration);
        assertEquals(List.of("http://localhost:5173"), configuration.getAllowedOrigins());
        assertEquals(
                List.of("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS"),
                configuration.getAllowedMethods());
        assertEquals(
                List.of("Authorization", "Content-Type", "Accept", "Origin"),
                configuration.getAllowedHeaders());
        assertEquals(Boolean.TRUE, configuration.getAllowCredentials());
    }

    @Test
    void corsConfigurationSource_whenOutsideApi_hasNoConfiguration() {
        var source = new SecurityConfiguration().corsConfigurationSource();
        var request = request("GET", "/private");

        var configuration = source.getCorsConfiguration(request);

        assertNull(configuration);
    }

    @Test
    void securityFilterChain_whenPreflightAllowed_returnsCorsHeaders() throws Exception {
        var request = request("OPTIONS", "/api/v1/vacancies");
        request.addHeader(HttpHeaders.ORIGIN, "http://localhost:5173");
        request.addHeader(HttpHeaders.ACCESS_CONTROL_REQUEST_METHOD, "POST");
        request.addHeader(
                HttpHeaders.ACCESS_CONTROL_REQUEST_HEADERS, "Authorization, Content-Type");
        var response = new MockHttpServletResponse();
        var downstream = mock(FilterChain.class);

        security.doFilter(request, response, downstream);

        assertEquals(200, response.getStatus());
        assertEquals(
                "http://localhost:5173",
                response.getHeader(HttpHeaders.ACCESS_CONTROL_ALLOW_ORIGIN));
        assertEquals("true", response.getHeader(HttpHeaders.ACCESS_CONTROL_ALLOW_CREDENTIALS));
        assertTrue(response.getHeader(HttpHeaders.ACCESS_CONTROL_ALLOW_METHODS).contains("POST"));
        verifyNoInteractions(downstream, provider);
    }

    @ParameterizedTest
    @ValueSource(strings = {"https://untrusted.example", "http://localhost:5174"})
    void securityFilterChain_whenOriginDisallowed_rejectsPreflight(String origin) throws Exception {
        var request = request("OPTIONS", "/api/v1/vacancies");
        request.addHeader(HttpHeaders.ORIGIN, origin);
        request.addHeader(HttpHeaders.ACCESS_CONTROL_REQUEST_METHOD, "POST");
        var response = new MockHttpServletResponse();
        var downstream = mock(FilterChain.class);

        security.doFilter(request, response, downstream);

        assertEquals(403, response.getStatus());
        assertNull(response.getHeader(HttpHeaders.ACCESS_CONTROL_ALLOW_ORIGIN));
        verifyNoInteractions(downstream, provider);
    }

    @Test
    void securityFilterChain_whenPreflightHeaderDisallowed_rejectsRequest() throws Exception {
        var request = request("OPTIONS", "/api/v1/vacancies");
        request.addHeader(HttpHeaders.ORIGIN, "http://localhost:5173");
        request.addHeader(HttpHeaders.ACCESS_CONTROL_REQUEST_METHOD, "POST");
        request.addHeader(HttpHeaders.ACCESS_CONTROL_REQUEST_HEADERS, "X-Unapproved");
        var response = new MockHttpServletResponse();
        var downstream = mock(FilterChain.class);

        security.doFilter(request, response, downstream);

        assertEquals(403, response.getStatus());
        verifyNoInteractions(downstream, provider);
    }
}
