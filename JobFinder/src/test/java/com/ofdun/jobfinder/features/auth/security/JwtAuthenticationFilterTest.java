package com.ofdun.jobfinder.features.auth.security;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import com.ofdun.jobfinder.features.auth.domain.jwt.JwtProvider;
import com.ofdun.jobfinder.features.auth.enums.AccountType;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import java.io.IOException;
import java.util.List;
import java.util.Locale;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.junit.jupiter.params.provider.NullSource;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpHeaders;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetails;

@Tag("equivalence")
@ExtendWith(MockitoExtension.class)
class JwtAuthenticationFilterTest {
    @Mock private JwtProvider provider;
    @Mock private FilterChain chain;
    private JwtAuthenticationFilter filter;
    private MockHttpServletRequest request;
    private MockHttpServletResponse response;

    @BeforeEach
    void setUp() {
        SecurityContextHolder.clearContext();
        filter = new JwtAuthenticationFilter(provider);
        request = new MockHttpServletRequest("GET", "/api/v1/applications");
        response = new MockHttpServletResponse();
    }

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    @ParameterizedTest
    @NullSource
    @ValueSource(strings = {"", "Basic credentials", "bearer token", "Bearer"})
    void doFilter_whenBearerHeaderMissing_continuesAnonymously(String header) throws Exception {
        if (header != null) {
            request.addHeader(HttpHeaders.AUTHORIZATION, header);
        }

        filter.doFilter(request, response, chain);

        assertNull(SecurityContextHolder.getContext().getAuthentication());
        assertNull(request.getAttribute(JwtAuthenticationFilter.ATTR_ACTOR_ID));
        assertNull(request.getAttribute(JwtAuthenticationFilter.ATTR_ACTOR_ROLE));
        verifyNoInteractions(provider);
        verify(chain).doFilter(request, response);
    }

    @ParameterizedTest
    @ValueSource(strings = {"Bearer ", "Bearer    "})
    void doFilter_whenTokenEmpty_continuesAnonymously(String header) throws Exception {
        request.addHeader(HttpHeaders.AUTHORIZATION, header);

        filter.doFilter(request, response, chain);

        assertNull(SecurityContextHolder.getContext().getAuthentication());
        verifyNoInteractions(provider);
        verify(chain).doFilter(request, response);
    }

    @ParameterizedTest
    @EnumSource(AccountType.class)
    void doFilter_whenTokenValid_setsIdentityAndRole(AccountType role) throws Exception {
        request.addHeader(HttpHeaders.AUTHORIZATION, "Bearer   signed-token  ");
        request.setRemoteAddr("127.0.0.2");
        when(provider.getAccountType("signed-token")).thenReturn(role);
        when(provider.validateToken("signed-token", role)).thenReturn(true);
        when(provider.getUserId("signed-token")).thenReturn(42L);

        filter.doFilter(request, response, chain);

        var authentication = SecurityContextHolder.getContext().getAuthentication();
        assertNotNull(authentication);
        assertTrue(authentication.isAuthenticated());
        assertEquals(new JobFinderPrincipal(42L, role), authentication.getPrincipal());
        assertNull(authentication.getCredentials());
        assertEquals(
                List.of("ROLE_" + role.name()),
                authentication.getAuthorities().stream().map(a -> a.getAuthority()).toList());
        var details = assertInstanceOf(WebAuthenticationDetails.class, authentication.getDetails());
        assertEquals("127.0.0.2", details.getRemoteAddress());
        assertNull(request.getSession(false));
        assertEquals(42L, request.getAttribute(JwtAuthenticationFilter.ATTR_ACTOR_ID));
        assertEquals(
                role.name().toLowerCase(Locale.ROOT),
                request.getAttribute(JwtAuthenticationFilter.ATTR_ACTOR_ROLE));
        verify(chain).doFilter(request, response);
    }

    @Test
    void doFilter_whenTokenInvalid_doesNotAuthenticate() throws Exception {
        request.addHeader(HttpHeaders.AUTHORIZATION, "Bearer invalid-token");
        when(provider.getAccountType("invalid-token")).thenReturn(AccountType.APPLICANT);
        when(provider.validateToken("invalid-token", AccountType.APPLICANT)).thenReturn(false);

        filter.doFilter(request, response, chain);

        assertNull(SecurityContextHolder.getContext().getAuthentication());
        assertNull(request.getAttribute(JwtAuthenticationFilter.ATTR_ACTOR_ID));
        assertNull(request.getAttribute(JwtAuthenticationFilter.ATTR_ACTOR_ROLE));
        verify(provider, never()).getUserId(anyString());
        verify(chain).doFilter(request, response);
    }

    @ParameterizedTest
    @ValueSource(strings = {"role", "validation", "subject"})
    void doFilter_whenProviderThrows_clearsIdentityAndContinues(String stage) throws Exception {
        request.addHeader(HttpHeaders.AUTHORIZATION, "Bearer broken-token");
        var previous =
                new UsernamePasswordAuthenticationToken(
                        new JobFinderPrincipal(99L, AccountType.EMPLOYER), null, List.of());
        SecurityContextHolder.getContext().setAuthentication(previous);
        request.setAttribute(JwtAuthenticationFilter.ATTR_ACTOR_ID, 99L);
        request.setAttribute(JwtAuthenticationFilter.ATTR_ACTOR_ROLE, "employer");
        var failure = new IllegalArgumentException("malformed token");
        switch (stage) {
            case "role" -> when(provider.getAccountType("broken-token")).thenThrow(failure);
            case "validation" -> {
                when(provider.getAccountType("broken-token")).thenReturn(AccountType.APPLICANT);
                when(provider.validateToken("broken-token", AccountType.APPLICANT))
                        .thenThrow(failure);
            }
            case "subject" -> {
                when(provider.getAccountType("broken-token")).thenReturn(AccountType.APPLICANT);
                when(provider.validateToken("broken-token", AccountType.APPLICANT))
                        .thenReturn(true);
                when(provider.getUserId("broken-token")).thenThrow(failure);
            }
        }

        filter.doFilter(request, response, chain);

        assertNull(SecurityContextHolder.getContext().getAuthentication());
        assertNull(request.getAttribute(JwtAuthenticationFilter.ATTR_ACTOR_ID));
        assertNull(request.getAttribute(JwtAuthenticationFilter.ATTR_ACTOR_ROLE));
        verify(chain).doFilter(request, response);
    }

    @Test
    void doFilter_whenValidatedRoleMissing_doesNotAuthenticate() throws Exception {
        request.addHeader(HttpHeaders.AUTHORIZATION, "Bearer token");
        when(provider.getAccountType("token")).thenReturn(null);
        when(provider.validateToken("token", null)).thenReturn(true);
        when(provider.getUserId("token")).thenReturn(42L);

        filter.doFilter(request, response, chain);

        assertNull(SecurityContextHolder.getContext().getAuthentication());
        assertNull(request.getAttribute(JwtAuthenticationFilter.ATTR_ACTOR_ID));
        assertNull(request.getAttribute(JwtAuthenticationFilter.ATTR_ACTOR_ROLE));
        verify(chain).doFilter(request, response);
    }

    @Test
    void doFilter_whenDownstreamThrowsServletException_propagatesFailure() throws Exception {
        request.addHeader(HttpHeaders.AUTHORIZATION, "Bearer token");
        when(provider.getAccountType("token")).thenReturn(AccountType.APPLICANT);
        when(provider.validateToken("token", AccountType.APPLICANT)).thenReturn(true);
        when(provider.getUserId("token")).thenReturn(42L);
        var failure = new ServletException("request failed");
        doThrow(failure).when(chain).doFilter(request, response);

        var thrown =
                assertThrows(
                        ServletException.class, () -> filter.doFilter(request, response, chain));

        assertSame(failure, thrown);
        verify(chain).doFilter(request, response);
    }

    @Test
    void doFilter_whenAnonymousDownstreamThrowsIOException_propagatesFailure() throws Exception {
        var failure = new IOException("connection closed");
        doThrow(failure).when(chain).doFilter(request, response);

        var thrown =
                assertThrows(IOException.class, () -> filter.doFilter(request, response, chain));

        assertSame(failure, thrown);
        verify(chain).doFilter(request, response);
    }
}
