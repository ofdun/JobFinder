package com.ofdun.jobfinder.features.api;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import com.ofdun.jobfinder.common.domain.model.PageResult;
import com.ofdun.jobfinder.features.applicant.exception.ApplicantNotFoundException;
import com.ofdun.jobfinder.features.auth.api.controller.AuthController;
import com.ofdun.jobfinder.features.auth.api.controller.UserAuthController;
import com.ofdun.jobfinder.features.auth.domain.model.TokenPair;
import com.ofdun.jobfinder.features.auth.domain.service.ApplicantAuthService;
import com.ofdun.jobfinder.features.auth.domain.service.EmployerAuthService;
import com.ofdun.jobfinder.features.auth.enums.AccountType;
import com.ofdun.jobfinder.features.auth.exception.InvalidRefreshTokenException;
import com.ofdun.jobfinder.features.location.api.controller.LocationController;
import com.ofdun.jobfinder.features.location.domain.service.LocationService;
import com.ofdun.jobfinder.features.resume.api.controller.ResumeController;
import com.ofdun.jobfinder.features.resume.domain.service.ResumeService;
import com.ofdun.jobfinder.features.vacancy.api.controller.VacancyController;
import com.ofdun.jobfinder.features.vacancy.domain.service.VacancyService;
import java.util.*;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.context.annotation.ClassPathScanningCandidateComponentProvider;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.type.filter.AnnotationTypeFilter;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.mvc.method.annotation.RequestMappingHandlerMapping;

class RestContractAlignmentTest {
    private final ApplicantAuthService applicants = mock(ApplicantAuthService.class);
    private final EmployerAuthService employers = mock(EmployerAuthService.class);

    private MockMvc mvc(Object... controllers) throws Exception {
        return MockMvcBuilders.standaloneSetup(controllers)
                .setControllerAdvice(
                        new GlobalExceptionHandler(),
                        new ApiContractAdvice(
                                new OpenApiContract(),
                                mock(
                                        jakarta.servlet.http.HttpServletRequest.class,
                                        invocation ->
                                                invocation
                                                        .getMethod()
                                                        .invoke(
                                                                ((org.springframework.web.context
                                                                                        .request
                                                                                        .ServletRequestAttributes)
                                                                                org.springframework
                                                                                        .web.context
                                                                                        .request
                                                                                        .RequestContextHolder
                                                                                        .currentRequestAttributes())
                                                                        .getRequest(),
                                                                invocation.getArguments()))),
                        new ApiResponseAdvice())
                .addInterceptors(new OpenApiContract())
                .build();
    }

    private <T> T controller(Class<T> type, Object... supplied) throws Exception {
        var constructor = type.getConstructors()[0];
        Object[] dependencies =
                Arrays.stream(constructor.getParameterTypes())
                        .map(
                                t ->
                                        Arrays.stream(supplied)
                                                .filter(t::isInstance)
                                                .findFirst()
                                                .orElseGet(() -> mock(t)))
                        .toArray();
        return type.cast(constructor.newInstance(dependencies));
    }

    @ParameterizedTest
    @EnumSource(AccountType.class)
    void unifiedLoginAndRefreshDispatchToTheSelectedRole(AccountType role) throws Exception {
        var service = role == AccountType.APPLICANT ? applicants : employers;
        when(service.login("test@example.com", "password"))
                .thenReturn(new TokenPair("access", "refresh"));
        when(service.refreshToken("refresh"))
                .thenReturn(new TokenPair("new-access", "new-refresh"));
        var mvc = mvc(new UserAuthController(applicants, employers));
        mvc.perform(
                        post("/api/v2/auth/login")
                                .contentType("application/json")
                                .content(
                                        "{\"email\":\"test@example.com\",\"password\":\"password\",\"userType\":\""
                                                + role
                                                + "\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.accessToken").value("access"))
                .andExpect(header().string("Cache-Control", "no-store"));
        mvc.perform(
                        post("/api/v2/auth/refresh")
                                .contentType("application/json")
                                .content(
                                        "{\"refreshToken\":\"refresh\",\"userType\":\""
                                                + role
                                                + "\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.refreshToken").value("new-refresh"));
        verify(service).login("test@example.com", "password");
        verify(service).refreshToken("refresh");
        verifyNoInteractions(role == AccountType.APPLICANT ? employers : applicants);
    }

    @ParameterizedTest
    @ValueSource(
            strings = {
                "",
                ",\"userType\":null",
                ",\"userType\":\"ADMIN\"",
                ",\"userType\":\"applicant\""
            })
    void loginAndRefreshRejectMissingOrInvalidRole(String role) throws Exception {
        var mvc = mvc(new UserAuthController(applicants, employers));
        mvc.perform(
                        post("/api/v2/auth/login")
                                .contentType("application/json")
                                .content(
                                        "{\"email\":\"test@example.com\",\"password\":\"password\""
                                                + role
                                                + "}"))
                .andExpect(status().isBadRequest());
        mvc.perform(
                        post("/api/v2/auth/refresh")
                                .contentType("application/json")
                                .content("{\"refreshToken\":\"refresh\"" + role + "}"))
                .andExpect(status().isBadRequest());
        verifyNoInteractions(applicants, employers);
    }

    @Test
    void invalidCredentialsAndRefreshRoleReturn401WithoutDetails() throws Exception {
        when(applicants.login(anyString(), anyString()))
                .thenThrow(new ApplicantNotFoundException("missing@example.com"));
        when(employers.refreshToken("applicant-refresh"))
                .thenThrow(new InvalidRefreshTokenException());
        var mvc = mvc(new UserAuthController(applicants, employers));
        mvc.perform(
                        post("/api/v2/auth/login")
                                .contentType("application/json")
                                .content(
                                        "{\"email\":\"missing@example.com\",\"password\":\"password\",\"userType\":\"APPLICANT\"}"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.stackTrace").isEmpty());
        mvc.perform(
                        post("/api/v2/auth/refresh")
                                .contentType("application/json")
                                .content(
                                        "{\"refreshToken\":\"applicant-refresh\",\"userType\":\"EMPLOYER\"}"))
                .andExpect(status().isUnauthorized())
                .andExpect(header().string("WWW-Authenticate", "Bearer"));
    }

    @Test
    void legacyAuthenticationStillAcceptsBodiesWithoutUserType() throws Exception {
        when(applicants.login(anyString(), anyString()))
                .thenReturn(new TokenPair("access", "refresh"));
        when(employers.refreshToken("refresh")).thenReturn(new TokenPair("access", "refresh"));
        var mvc = mvc(new AuthController(applicants, employers));
        mvc.perform(
                        post("/api/v1/auth/applicant/login")
                                .contentType("application/json")
                                .content(
                                        "{\"email\":\"test@example.com\",\"password\":\"password\"}"))
                .andExpect(status().isOk());
        mvc.perform(
                        post("/api/v1/auth/employer/refresh")
                                .contentType("application/json")
                                .content("{\"refreshToken\":\"refresh\"}"))
                .andExpect(status().isOk());
    }

    @Test
    void collectionRoutesKeepFiltersAndValidatePaginationAndSort() throws Exception {
        var vacancies = mock(VacancyService.class);
        var resumes = mock(ResumeService.class);
        var locations = mock(LocationService.class);
        when(vacancies.searchVacancies(any(), any()))
                .thenReturn(new PageResult<>(List.of(), 0, 20, 0L, 0));
        when(resumes.searchResumes(any(), any()))
                .thenReturn(new PageResult<>(List.of(), 0, 20, 0L, 0));
        when(locations.searchLocations("Москва", 20)).thenReturn(List.of());
        var mvc =
                mvc(
                        controller(VacancyController.class, vacancies),
                        controller(ResumeController.class, resumes),
                        new LocationController(locations));
        mvc.perform(get("/api/v2/vacancies").param("sortBy", "salary").param("q", "Java"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.items").isEmpty());
        verify(vacancies)
                .searchVacancies(
                        argThat(f -> "Java".equals(f.getQ())),
                        argThat(p -> "salary".equals(p.getSortBy())));
        mvc.perform(get("/api/v2/resumes").param("applicantId", "1")).andExpect(status().isOk());
        mvc.perform(get("/api/v2/locations").param("q", "Москва")).andExpect(status().isOk());
        mvc.perform(get("/api/v2/locations")).andExpect(status().isBadRequest());
        mvc.perform(get("/api/v2/vacancies").param("limit", "0"))
                .andExpect(status().isBadRequest());
        mvc.perform(get("/api/v2/vacancies").param("sortBy", "password"))
                .andExpect(status().isBadRequest());
        mvc.perform(get("/api/v1/vacancies/search")).andExpect(status().isOk());
    }

    @Test
    void everyDocumentedOperationHasAControllerMapping() throws Exception {
        var scanner = new ClassPathScanningCandidateComponentProvider(false);
        scanner.addIncludeFilter(new AnnotationTypeFilter(RestController.class));
        var controllers = new ArrayList<Object>();
        for (var candidate : scanner.findCandidateComponents("com.ofdun.jobfinder.features"))
            controllers.add(controller(Class.forName(candidate.getBeanClassName())));
        var mvc = mvc(controllers.toArray());
        var mapping =
                mvc.getDispatcherServlet()
                        .getWebApplicationContext()
                        .getBean(RequestMappingHandlerMapping.class);
        var routes = new HashSet<String>();
        mapping.getHandlerMethods()
                .forEach(
                        (info, method) -> {
                            for (String path : info.getPatternValues())
                                for (var verb : info.getMethodsCondition().getMethods())
                                    routes.add(verb.name() + " " + path);
                        });
        var document =
                new ObjectMapper(new YAMLFactory())
                        .readTree(
                                new ClassPathResource("static/api/v2/openapi.yml")
                                        .getInputStream());
        for (var path : document.path("paths").properties())
            for (var method : path.getValue().properties())
                if (Set.of("get", "post", "put", "patch", "delete").contains(method.getKey()))
                    assertTrue(
                            routes.contains(
                                    method.getKey().toUpperCase(Locale.ROOT) + " " + path.getKey()),
                            method.getKey() + " " + path.getKey());
    }

    @ParameterizedTest
    @org.junit.jupiter.api.Tag("boundary")
    @org.junit.jupiter.params.provider.CsvSource({
        "limit,0", "limit,101", "limit,invalid", "offset,-1",
        "employerId,invalid", "locationId,9223372036854775808", "sortDesc,yes", "status,DELETED"
    })
    void invalidCollectionParametersNeverReachTheService(String name, String value)
            throws Exception {
        var vacancies = mock(VacancyService.class);
        mvc(controller(VacancyController.class, vacancies))
                .perform(get("/api/v2/vacancies").param(name, value))
                .andExpect(status().isBadRequest());
        verifyNoInteractions(vacancies);
    }

    @ParameterizedTest
    @org.junit.jupiter.api.Tag("boundary")
    @ValueSource(ints = {1, 100})
    void collectionAcceptsBothPaginationBoundaries(int limit) throws Exception {
        var vacancies = mock(VacancyService.class);
        when(vacancies.searchVacancies(any(), any()))
                .thenReturn(new PageResult<>(List.of(), 0, limit, 0L, 0));
        mvc(controller(VacancyController.class, vacancies))
                .perform(
                        get("/api/v2/vacancies")
                                .param("limit", String.valueOf(limit))
                                .param("offset", "0"))
                .andExpect(status().isOk());
        verify(vacancies)
                .searchVacancies(any(), argThat(p -> p.getLimit() == limit && p.getOffset() == 0));
    }
}
