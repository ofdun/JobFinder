package com.ofdun.jobfinder.features.graphql;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.ofdun.jobfinder.features.api.ApiAccess;
import com.ofdun.jobfinder.features.applicant.api.mapper.ApplicantApiMapper;
import com.ofdun.jobfinder.features.applicant.domain.service.ApplicantService;
import com.ofdun.jobfinder.features.application.api.mapper.ApplicationApiMapper;
import com.ofdun.jobfinder.features.application.domain.model.ApplicationModel;
import com.ofdun.jobfinder.features.application.domain.service.ApplicationService;
import com.ofdun.jobfinder.features.application.enums.ApplicationStatus;
import com.ofdun.jobfinder.features.auth.domain.service.*;
import com.ofdun.jobfinder.features.auth.enums.AccountType;
import com.ofdun.jobfinder.features.auth.security.JobFinderPrincipal;
import com.ofdun.jobfinder.features.category.domain.model.CategoryModel;
import com.ofdun.jobfinder.features.category.domain.service.CategoryService;
import com.ofdun.jobfinder.features.employer.api.mapper.EmployerApiMapper;
import com.ofdun.jobfinder.features.employer.domain.service.EmployerService;
import com.ofdun.jobfinder.features.encrypt.NoEncryptionService;
import com.ofdun.jobfinder.features.language.domain.service.LanguageService;
import com.ofdun.jobfinder.features.location.domain.service.LocationService;
import com.ofdun.jobfinder.features.matching.domain.service.MatchingService;
import com.ofdun.jobfinder.features.resume.api.mapper.ResumeApiMapper;
import com.ofdun.jobfinder.features.resume.domain.model.*;
import com.ofdun.jobfinder.features.resume.domain.repository.RelationalResumeRepository;
import com.ofdun.jobfinder.features.resume.domain.service.ResumeService;
import com.ofdun.jobfinder.features.skill.domain.service.SkillService;
import com.ofdun.jobfinder.features.vacancy.api.mapper.*;
import com.ofdun.jobfinder.features.vacancy.domain.model.*;
import com.ofdun.jobfinder.features.vacancy.domain.service.*;
import graphql.GraphQL;
import jakarta.validation.Validation;
import java.util.*;
import org.junit.jupiter.api.*;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

class GraphQlHttpTest {
    private final ObjectMapper json = new ObjectMapper();
    private final ApplicantService applicants = mock(ApplicantService.class);
    private final EmployerService employers = mock(EmployerService.class);
    private final VacancyService vacancies = mock(VacancyService.class);
    private final DraftVacancyService drafts = mock(DraftVacancyService.class);
    private final ResumeService resumes = mock(ResumeService.class);
    private final RelationalResumeRepository resumeRepository =
            mock(RelationalResumeRepository.class);
    private final ApplicationService applications = mock(ApplicationService.class);
    private final ApplicantAuthService applicantAuth = mock(ApplicantAuthService.class);
    private final EmployerAuthService employerAuth = mock(EmployerAuthService.class);
    private final CategoryService categories = mock(CategoryService.class);
    private final SkillService skills = mock(SkillService.class);
    private final LanguageService languages = mock(LanguageService.class);
    private final LocationService locations = mock(LocationService.class);
    private final MatchingService matching = mock(MatchingService.class);
    private static final jakarta.validation.ValidatorFactory validators =
            Validation.buildDefaultValidatorFactory();

    @AfterAll
    static void closeValidators() {
        validators.close();
    }

    private MockMvc mvc;
    private GraphQL graphQL;

    @BeforeEach
    void setup() throws Exception {
        var operations =
                new GraphQlOperations(
                        new ApiAccess(vacancies, resumeRepository, applications),
                        applicants,
                        employers,
                        vacancies,
                        drafts,
                        resumes,
                        applications,
                        applicantAuth,
                        employerAuth,
                        categories,
                        skills,
                        languages,
                        locations,
                        matching,
                        new ApplicantApiMapper(new NoEncryptionService()),
                        new EmployerApiMapper(new NoEncryptionService()),
                        new VacancyApiMapper(),
                        new ResumeApiMapper(),
                        new ApplicationApiMapper(),
                        new DraftVacancyApiMapper(),
                        validators.getValidator());
        graphQL = new GraphQlConfiguration().graphQL(operations);
        mvc = MockMvcBuilders.standaloneSetup(new GraphQlController(graphQL, json)).build();
    }

    private org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder operation(
            String query, String name, Map<String, Object> vars) throws Exception {
        return post("/api/v2")
                .contentType("application/json")
                .accept("application/graphql-response+json")
                .content(
                        json.writeValueAsString(
                                Map.of("query", query, "operationName", name, "variables", vars)));
    }

    private UsernamePasswordAuthenticationToken actor(AccountType type, long id) {
        return new UsernamePasswordAuthenticationToken(
                new JobFinderPrincipal(id, type), null, List.of());
    }

    private ResumeModel resume(long id, long owner) {
        var r = new ResumeModel(id);
        r.setApplicantId(owner);
        r.setCategoryId(1L);
        r.setDate(new Date(0));
        return r;
    }

    private VacancyModel vacancy(long id, long owner) {
        var v = new VacancyModel();
        v.setId(id);
        v.setEmployerId(owner);
        return v;
    }

    @Test
    void publicAndDeniedFieldsProduceIndependentResults() throws Exception {
        when(categories.getAllCategories())
                .thenReturn(List.of(new CategoryModel(1L, "Engineering")));
        mvc.perform(
                        operation(
                                        "query Q { categories { id name } employer(id:\"2\") {"
                                                + " email } }",
                                        "Q",
                                        Map.of())
                                .principal(actor(AccountType.APPLICANT, 1)))
                .andExpect(status().isOk())
                .andExpect(header().string("Cache-Control", "no-store"))
                .andExpect(jsonPath("$.data.categories[0].id").value("1"))
                .andExpect(jsonPath("$.data.employer").isEmpty())
                .andExpect(jsonPath("$.errors[0].extensions.code").value("FORBIDDEN"));
        verifyNoInteractions(employers);
    }

    @Test
    void missingAuthenticationIsAnExecutionError() throws Exception {
        mvc.perform(operation("query Q { myApplications { id } }", "Q", Map.of()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.errors[0].extensions.code").value("UNAUTHENTICATED"));
        verifyNoInteractions(applications);
    }

    @Test
    void invalidBearerIsRejectedBeforeAnyExecution() throws Exception {
        mvc.perform(
                        operation("query Q { categories { id } }", "Q", Map.of())
                                .header("Authorization", "Bearer invalid"))
                .andExpect(status().isUnauthorized())
                .andExpect(header().string("WWW-Authenticate", "Bearer"));
        verifyNoInteractions(categories);
    }

    @Test
    void mediaMethodAndEnvelopeRulesAreEnforced() throws Exception {
        mvc.perform(get("/api/v2"))
                .andExpect(status().isMethodNotAllowed())
                .andExpect(header().string("Allow", "POST"));
        mvc.perform(post("/api/v2").contentType("text/plain").content("{}"))
                .andExpect(status().isUnsupportedMediaType());
        mvc.perform(
                        post("/api/v2")
                                .contentType("application/json")
                                .accept("text/html")
                                .content("{}"))
                .andExpect(status().isNotAcceptable());
        for (String body : List.of("[]", "{broken", "{\"query\":\"{ categories { id } }\"}"))
            mvc.perform(post("/api/v2").contentType("application/json").content(body))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.data").doesNotExist());
    }

    @Test
    void validationAndVariableCoercionDoNotRunResolvers() throws Exception {
        mvc.perform(operation("query Q { unknown }", "Q", Map.of()))
                .andExpect(status().isBadRequest())
                .andExpect(
                        jsonPath("$.errors[0].extensions.code").value("GRAPHQL_VALIDATION_FAILED"));
        mvc.perform(
                        operation(
                                "query Q($n: Int) { locations(q:\"Moscow\", limit:$n) { id } }",
                                "Q",
                                Map.of("n", "ten")))
                .andExpect(status().isBadRequest());
        mvc.perform(operation("query Q { categories { id } }", "Missing", Map.of()))
                .andExpect(status().isBadRequest());
        verifyNoInteractions(categories, locations);
    }

    @Test
    void customScalarVariablesRejectInvalidFormats() throws Exception {
        String query = "query Q($f: VacancyFilter) { vacancies(filter:$f) { size } }";
        for (Map<String, Object> invalid :
                List.of(
                        Map.<String, Object>of("salaryMin", "01"),
                        Map.<String, Object>of("salaryMin", 12),
                        Map.<String, Object>of("publicationDateFrom", "2026-02-30T10:00:00Z")))
            mvc.perform(operation(query, "Q", Map.of("f", invalid)))
                    .andExpect(status().isBadRequest());
        verifyNoInteractions(vacancies);
    }

    @Test
    void fieldAndDepthLimitsRejectBeforeExecutionIncludingFragments() throws Exception {
        String fields =
                java.util.stream.IntStream.range(0, 201)
                        .mapToObj(i -> "c" + i + ":categories{id}")
                        .collect(java.util.stream.Collectors.joining(" "));
        mvc.perform(operation("query Q { " + fields + " }", "Q", Map.of()))
                .andExpect(status().isBadRequest());
        String nested =
                "query Q { __schema { types { fields { type { ofType { ofType { ofType { ofType {"
                        + " name } } } } } } } } }";
        mvc.perform(operation(nested, "Q", Map.of())).andExpect(status().isBadRequest());
        verifyNoInteractions(categories);
    }

    @Test
    void applicationCreationChecksOwnerAndUsesNewStatus() throws Exception {
        when(resumeRepository.getResumeById(20L)).thenReturn(Optional.of(resume(20, 1)));
        when(vacancies.getVacancyById(10L)).thenReturn(vacancy(10, 2));
        when(applications.getApplicationsByVacancyId(10L)).thenReturn(List.of());
        when(applications.saveApplication(any())).thenReturn(40L);
        when(applications.getApplication(40L))
                .thenReturn(
                        new ApplicationModel(40L, 10L, 20L, new Date(0), ApplicationStatus.NEW));
        mvc.perform(
                        operation(
                                        "mutation M {"
                                            + " createApplication(input:{vacancyId:\"10\",resumeId:\"20\"})"
                                            + " { id applicationStatus applicationDate } }",
                                        "M",
                                        Map.of())
                                .principal(actor(AccountType.APPLICANT, 1)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.createApplication.id").value("40"))
                .andExpect(jsonPath("$.data.createApplication.applicationStatus").value("NEW"));
        verify(applications)
                .saveApplication(
                        argThat(
                                a ->
                                        a.getApplicationStatus() == ApplicationStatus.NEW
                                                && a.getApplicationDate() != null));
    }

    @Test
    void foreignResumeAndDuplicateApplicationCannotBeCreated() throws Exception {
        when(resumeRepository.getResumeById(20L)).thenReturn(Optional.of(resume(20, 3)));
        var query =
                "mutation M { createApplication(input:{vacancyId:\"10\",resumeId:\"20\"}) { id } }";
        mvc.perform(operation(query, "M", Map.of()).principal(actor(AccountType.APPLICANT, 1)))
                .andExpect(jsonPath("$.errors[0].extensions.code").value("FORBIDDEN"));
        when(resumeRepository.getResumeById(20L)).thenReturn(Optional.of(resume(20, 1)));
        when(vacancies.getVacancyById(10L)).thenReturn(vacancy(10, 2));
        when(applications.getApplicationsByVacancyId(10L))
                .thenReturn(
                        List.of(
                                new ApplicationModel(
                                        40L, 10L, 20L, new Date(), ApplicationStatus.NEW)));
        mvc.perform(operation(query, "M", Map.of()).principal(actor(AccountType.APPLICANT, 1)))
                .andExpect(jsonPath("$.errors[0].extensions.code").value("CONFLICT"));
        verify(applications, never()).saveApplication(any());
    }

    @Test
    void statusChangePreservesRelationshipsAndDateAndRejectsApplicant() throws Exception {
        var original = new ApplicationModel(40L, 10L, 20L, new Date(0), ApplicationStatus.NEW);
        when(applications.getApplication(40L)).thenReturn(original);
        when(vacancies.getVacancyById(10L)).thenReturn(vacancy(10, 2));
        when(applications.updateApplication(any())).thenAnswer(i -> i.getArgument(0));
        String query =
                "mutation M { updateApplicationStatus(id:\"40\",status:INVITATION) { id vacancyId"
                        + " resumeId applicationDate applicationStatus } }";
        mvc.perform(operation(query, "M", Map.of()).principal(actor(AccountType.APPLICANT, 1)))
                .andExpect(jsonPath("$.errors[0].extensions.code").value("FORBIDDEN"));
        mvc.perform(operation(query, "M", Map.of()).principal(actor(AccountType.EMPLOYER, 2)))
                .andExpect(
                        jsonPath("$.data.updateApplicationStatus.applicationDate")
                                .value("1970-01-01T00:00:00Z"))
                .andExpect(jsonPath("$.data.updateApplicationStatus.vacancyId").value("10"));
        verify(applications)
                .updateApplication(
                        argThat(
                                a ->
                                        a.getApplicationDate().equals(new Date(0))
                                                && a.getResumeId() == 20L));
    }

    @Test
    void nestedResumeRechecksAccessEvenAfterParentAccess() throws Exception {
        when(applications.getApplication(40L))
                .thenReturn(new ApplicationModel(40L, 10L, 20L, new Date(), ApplicationStatus.NEW));
        when(resumeRepository.getResumeById(20L))
                .thenReturn(Optional.of(resume(20, 1)), Optional.of(resume(20, 3)));
        mvc.perform(
                        operation(
                                        "query Q { application(id:\"40\") { id resume { id } }"
                                                + " categories { id } }",
                                        "Q",
                                        Map.of())
                                .principal(actor(AccountType.APPLICANT, 1)))
                .andExpect(jsonPath("$.data.application").isEmpty())
                .andExpect(jsonPath("$.errors[0].extensions.code").value("FORBIDDEN"));
        verifyNoInteractions(resumes);
    }

    @Test
    void replacementClearsOptionalTextAndListsWithoutChangingOwner() throws Exception {
        var original = resume(20, 1);
        original.setDescription("old");
        when(resumeRepository.getResumeById(20L)).thenReturn(Optional.of(original));
        when(resumes.replaceResume(any())).thenAnswer(i -> Optional.of(i.getArgument(0)));
        when(resumes.getResumeById(20L)).thenReturn(Optional.of(resume(20, 1)));
        when(categories.getCategoryById(1L)).thenReturn(new CategoryModel(1L, "Engineering"));
        mvc.perform(
                        operation(
                                        "mutation M {"
                                            + " replaceResume(id:\"20\",input:{applicantId:\"1\",categoryId:\"1\",skillIds:[],languageIds:[],educations:[],jobExperiences:[]})"
                                            + " { id description educations { id } } }",
                                        "M",
                                        Map.of())
                                .principal(actor(AccountType.APPLICANT, 1)))
                .andExpect(jsonPath("$.data.replaceResume.description").isEmpty())
                .andExpect(jsonPath("$.data.replaceResume.educations").isArray());
        verify(resumes)
                .replaceResume(
                        argThat(
                                r ->
                                        r.getDescription() == null
                                                && r.getEducations().isEmpty()
                                                && r.getApplicantId() == 1L));
    }

    @org.junit.jupiter.params.ParameterizedTest
    @org.junit.jupiter.params.provider.EnumSource(AccountType.class)
    void loginAndRefreshUseSelectedRoleWithoutBearer(AccountType role) throws Exception {
        var service = role == AccountType.APPLICANT ? applicantAuth : employerAuth;
        var other = role == AccountType.APPLICANT ? employerAuth : applicantAuth;
        when(service.login("person@example.com", "password"))
                .thenReturn(
                        new com.ofdun.jobfinder.features.auth.domain.model.TokenPair(
                                "access", "refresh"));
        when(service.refreshToken("refresh"))
                .thenReturn(
                        new com.ofdun.jobfinder.features.auth.domain.model.TokenPair(
                                "new-access", "new-refresh"));
        mvc.perform(
                        operation(
                                "mutation M { login(input:{accountType:"
                                        + role
                                        + ",email:\"person@example.com\",password:\"password\"}) {"
                                        + " accessToken refreshToken } }",
                                "M",
                                Map.of()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.errors").doesNotExist())
                .andExpect(jsonPath("$.data.login.accessToken").value("access"));
        mvc.perform(
                        operation(
                                "mutation M { refreshTokens(input:{accountType:"
                                        + role
                                        + ",refreshToken:\"refresh\"}) { accessToken refreshToken }"
                                        + " }",
                                "M",
                                Map.of()))
                .andExpect(jsonPath("$.errors").doesNotExist())
                .andExpect(jsonPath("$.data.refreshTokens.refreshToken").value("new-refresh"));
        verifyNoInteractions(other);
    }

    @org.junit.jupiter.params.ParameterizedTest
    @org.junit.jupiter.params.provider.ValueSource(
            strings = {"Applicant", "Employer", "Vacancy", "Resume", "Application"})
    void deletionChecksRoleAndOwnershipBeforeChangingData(String resource) throws Exception {
        boolean employer = resource.equals("Employer") || resource.equals("Vacancy");
        var role = employer ? AccountType.EMPLOYER : AccountType.APPLICANT;
        when(vacancies.getVacancyById(20L)).thenReturn(vacancy(20, 1));
        when(resumeRepository.getResumeById(20L)).thenReturn(Optional.of(resume(20, 1)));
        when(applications.getApplication(20L))
                .thenReturn(
                        new ApplicationModel(20L, 20L, 20L, new Date(0), ApplicationStatus.NEW));
        // Profiles are addressed by account ID; related resources use their own IDs.
        long id = resource.equals("Applicant") || resource.equals("Employer") ? 1 : 20;
        String field = "delete" + resource;
        String query = "mutation M { " + field + "(id:\"" + id + "\") { id } }";
        mvc.perform(operation(query, "M", Map.of()).principal(actor(role, 2)))
                .andExpect(jsonPath("$.errors[0].extensions.code").value("FORBIDDEN"));
        mvc.perform(
                        operation(query, "M", Map.of())
                                .principal(
                                        actor(
                                                employer
                                                        ? AccountType.APPLICANT
                                                        : AccountType.EMPLOYER,
                                                1)))
                .andExpect(jsonPath("$.errors[0].extensions.code").value("FORBIDDEN"));
        verify(applicants, never()).deleteApplicant(any());
        verify(employers, never()).deleteEmployer(any());
        verify(vacancies, never()).deleteVacancy(any());
        verify(resumes, never()).deleteResume(any());
        verify(applications, never()).deleteApplication(any());
        mvc.perform(operation(query, "M", Map.of()).principal(actor(role, 1)))
                .andExpect(jsonPath("$.errors").doesNotExist())
                .andExpect(jsonPath("$.data." + field + ".id").value(String.valueOf(id)));
        switch (resource) {
            case "Applicant" -> verify(applicants).deleteApplicant(id);
            case "Employer" -> verify(employers).deleteEmployer(id);
            case "Vacancy" -> verify(vacancies).deleteVacancy(id);
            case "Resume" -> verify(resumes).deleteResume(id);
            case "Application" -> verify(applications).deleteApplication(id);
        }
    }

    @Test
    void applicantResumeSearchForcesOwnIdAndRejectsForeignFilter() throws Exception {
        when(resumes.searchResumes(any(), any()))
                .thenReturn(
                        new com.ofdun.jobfinder.common.domain.model.PageResult<>(
                                List.of(), 0, 20, 0L, 0));
        mvc.perform(
                        operation(
                                        "query Q { resumes { items { id } totalElements } }",
                                        "Q",
                                        Map.of())
                                .principal(actor(AccountType.APPLICANT, 7)))
                .andExpect(jsonPath("$.errors").doesNotExist());
        verify(resumes)
                .searchResumes(argThat(f -> Long.valueOf(7).equals(f.getApplicantId())), any());
        clearInvocations(resumes);
        mvc.perform(
                        operation(
                                        "query Q { resumes(filter:{applicantId:\"8\"}) { items { id"
                                                + " } } }",
                                        "Q",
                                        Map.of())
                                .principal(actor(AccountType.APPLICANT, 7)))
                .andExpect(jsonPath("$.errors[0].extensions.code").value("FORBIDDEN"));
        verifyNoInteractions(resumes);
    }

    @Test
    void employerResumeSearchKeepsFilterAndSort() throws Exception {
        when(resumes.searchResumes(any(), any()))
                .thenReturn(
                        new com.ofdun.jobfinder.common.domain.model.PageResult<>(
                                List.of(), 1, 5, 0L, 0));
        mvc.perform(
                        operation(
                                        "query Q {"
                                            + " resumes(filter:{applicantId:\"8\"},page:{limit:5,offset:5},sort:ID,direction:ASC)"
                                            + " { size } }",
                                        "Q",
                                        Map.of())
                                .principal(actor(AccountType.EMPLOYER, 7)))
                .andExpect(jsonPath("$.errors").doesNotExist());
        verify(resumes)
                .searchResumes(
                        argThat(f -> Long.valueOf(8).equals(f.getApplicantId())),
                        argThat(
                                p ->
                                        p.getLimit() == 5
                                                && p.getOffset() == 5
                                                && "id".equals(p.getSortBy())
                                                && !p.isSortDesc()));
    }

    @org.junit.jupiter.params.ParameterizedTest
    @org.junit.jupiter.params.provider.ValueSource(
            strings = {
                "page:{limit:0}",
                "page:{limit:101}",
                "page:{offset:-1}",
                "filter:{salaryMin:\"20\",salaryMax:\"10\"}",
                "filter:{publicationDateFrom:\"2026-10-02T00:00:00Z\",publicationDateTo:\"2026-10-01T00:00:00Z\"}"
            })
    @Tag("boundary")
    void invalidVacancySearchDoesNotReachStorage(String args) throws Exception {
        mvc.perform(operation("query Q { vacancies(" + args + ") { size } }", "Q", Map.of()))
                .andExpect(jsonPath("$.errors[0].extensions.code").value("BAD_USER_INPUT"));
        verifyNoInteractions(vacancies);
    }

    @Test
    void vacancySearchKeepsDecimalPrecisionAndFilterPaginationSeparation() throws Exception {
        when(vacancies.searchVacancies(any(), any()))
                .thenReturn(
                        new com.ofdun.jobfinder.common.domain.model.PageResult<>(
                                List.of(), 1, 5, 0L, 0));
        mvc.perform(
                        operation(
                                "query Q {"
                                    + " vacancies(filter:{salaryMin:\"100000.01\",employerId:\"2\"},page:{limit:5,offset:5},sort:SALARY,direction:DESC)"
                                    + " { size } }",
                                "Q",
                                Map.of()))
                .andExpect(jsonPath("$.errors").doesNotExist());
        verify(vacancies)
                .searchVacancies(
                        argThat(
                                f ->
                                        new java.math.BigDecimal("100000.01")
                                                        .equals(f.getSalaryMin())
                                                && Long.valueOf(2).equals(f.getEmployerId())),
                        argThat(
                                p ->
                                        p.getLimit() == 5
                                                && p.getOffset() == 5
                                                && p.isSortDesc()
                                                && "salary".equals(p.getSortBy())));
    }

    @Test
    void draftPaginationUsesTimestampThenIdAndProtectsForeignVacancy() throws Exception {
        when(vacancies.getVacancyById(10L)).thenReturn(vacancy(10, 2));
        when(drafts.getDrafts(10L))
                .thenReturn(
                        List.of(
                                new DraftVacancyModel(1L, 10L, new Date(1000), "{}"),
                                new DraftVacancyModel(3L, 10L, new Date(2000), "{}"),
                                new DraftVacancyModel(2L, 10L, new Date(2000), "{}")));
        String query = "query Q { vacancyDrafts(vacancyId:\"10\",page:{limit:1,offset:1}) { id } }";
        mvc.perform(operation(query, "Q", Map.of()).principal(actor(AccountType.EMPLOYER, 3)))
                .andExpect(jsonPath("$.errors[0].extensions.code").value("FORBIDDEN"));
        verifyNoInteractions(drafts);
        mvc.perform(operation(query, "Q", Map.of()).principal(actor(AccountType.EMPLOYER, 2)))
                .andExpect(jsonPath("$.errors").doesNotExist())
                .andExpect(jsonPath("$.data.vacancyDrafts.length()").value(1))
                .andExpect(jsonPath("$.data.vacancyDrafts[0].id").value("2"));
    }

    @org.junit.jupiter.params.ParameterizedTest
    @org.junit.jupiter.params.provider.ValueSource(strings = {"vacancyId", "resumeId"})
    void applicationReplacementCannotMoveRelationships(String field) throws Exception {
        when(applications.getApplication(40L))
                .thenReturn(
                        new ApplicationModel(40L, 10L, 20L, new Date(0), ApplicationStatus.NEW));
        when(vacancies.getVacancyById(10L)).thenReturn(vacancy(10, 2));
        String vacancyId = field.equals("vacancyId") ? "11" : "10";
        String resumeId = field.equals("resumeId") ? "21" : "20";
        mvc.perform(
                        operation(
                                        "mutation M {"
                                            + " replaceApplication(id:\"40\",input:{vacancyId:\""
                                                + vacancyId
                                                + "\",resumeId:\""
                                                + resumeId
                                                + "\",applicationStatus:INVITATION}) { id } }",
                                        "M",
                                        Map.of())
                                .principal(actor(AccountType.EMPLOYER, 2)))
                .andExpect(jsonPath("$.errors[0].extensions.code").value("CONFLICT"));
        verify(applications, never()).updateApplication(any());
    }

    @Test
    void missingResumeIsNotFoundAndInternalErrorsDoNotLeakDetails() throws Exception {
        when(resumeRepository.getResumeById(20L)).thenReturn(Optional.empty());
        mvc.perform(
                        operation("query Q { resume(id:\"20\") { id } }", "Q", Map.of())
                                .principal(actor(AccountType.APPLICANT, 1)))
                .andExpect(jsonPath("$.errors[0].extensions.code").value("NOT_FOUND"));
        when(categories.getAllCategories()).thenThrow(new IllegalStateException("password=secret"));
        mvc.perform(operation("query Q { categories { id } }", "Q", Map.of()))
                .andExpect(jsonPath("$.errors[0].extensions.code").value("INTERNAL_SERVER_ERROR"))
                .andExpect(
                        content()
                                .string(
                                        org.hamcrest.Matchers.not(
                                                org.hamcrest.Matchers.containsString(
                                                        "password=secret"))));
    }

    @org.junit.jupiter.params.ParameterizedTest
    @org.junit.jupiter.params.provider.EnumSource(AccountType.class)
    void profileRegistrationAndReplacementKeepPasswordPrivateAndPreserveOmittedPassword(
            AccountType role) throws Exception {
        String resource = role == AccountType.APPLICANT ? "Applicant" : "Employer";
        when(locations.getLocationById(any()))
                .thenReturn(com.ofdun.jobfinder.support.TestDataMother.location());
        if (role == AccountType.APPLICANT) {
            var model = com.ofdun.jobfinder.support.TestDataMother.applicant();
            model.setId(1L);
            model.setPasswordHash("stored-secret-hash");
            when(applicants.createApplicant(any())).thenReturn(1L);
            when(applicants.getApplicantById(1L)).thenReturn(model);
        } else {
            var model = com.ofdun.jobfinder.support.TestDataMother.employer();
            model.setId(1L);
            model.setPasswordHash("stored-secret-hash");
            when(employers.createEmployer(any())).thenReturn(1L);
            when(employers.getEmployerById(1L)).thenReturn(model);
        }
        mvc.perform(
                        operation(
                                "mutation M { register"
                                        + resource
                                        + "(input:{name:\"Test"
                                        + " User\",email:\"test@example.com\",password:\"password\",locationId:\"1\"})"
                                        + " { id name email } }",
                                "M",
                                Map.of()))
                .andExpect(jsonPath("$.errors").doesNotExist())
                .andExpect(jsonPath("$.data.register" + resource + ".id").value("1"))
                .andExpect(
                        content()
                                .string(
                                        org.hamcrest.Matchers.not(
                                                org.hamcrest.Matchers.containsString(
                                                        "stored-secret-hash"))));
        String replacement =
                "mutation M { replace"
                        + resource
                        + "(id:\"1\",input:{name:\"Changed"
                        + " User\",email:\"test@example.com\",locationId:\"1\"}) { id name } }";
        mvc.perform(operation(replacement, "M", Map.of()).principal(actor(role, 2)))
                .andExpect(jsonPath("$.errors[0].extensions.code").value("FORBIDDEN"));
        verify(applicants, never()).updateApplicant(any());
        verify(employers, never()).updateEmployer(any());
        mvc.perform(operation(replacement, "M", Map.of()).principal(actor(role, 1)))
                .andExpect(jsonPath("$.errors").doesNotExist());
        if (role == AccountType.APPLICANT)
            verify(applicants)
                    .updateApplicant(
                            argThat(
                                    m ->
                                            m.getId() == 1L
                                                    && "Changed User".equals(m.getName())
                                                    && "stored-secret-hash"
                                                            .equals(m.getPasswordHash())));
        else
            verify(employers)
                    .updateEmployer(
                            argThat(
                                    m ->
                                            m.getId() == 1L
                                                    && "Changed User".equals(m.getName())
                                                    && "stored-secret-hash"
                                                            .equals(m.getPasswordHash())));
    }

    @Test
    void matchingRequiresVacancyOwnerAndPassesBoundedLimit() throws Exception {
        when(vacancies.getVacancyById(10L)).thenReturn(vacancy(10, 2));
        when(matching.findSuitableCandidates(10L, 5)).thenReturn(List.of());
        String query = "query Q { matchingCandidates(vacancyId:\"10\",limit:5) { id score } }";
        mvc.perform(operation(query, "Q", Map.of()).principal(actor(AccountType.APPLICANT, 2)))
                .andExpect(jsonPath("$.errors[0].extensions.code").value("FORBIDDEN"));
        mvc.perform(operation(query, "Q", Map.of()).principal(actor(AccountType.EMPLOYER, 3)))
                .andExpect(jsonPath("$.errors[0].extensions.code").value("FORBIDDEN"));
        verifyNoInteractions(matching);
        mvc.perform(operation(query, "Q", Map.of()).principal(actor(AccountType.EMPLOYER, 2)))
                .andExpect(jsonPath("$.errors").doesNotExist())
                .andExpect(jsonPath("$.data.matchingCandidates").isEmpty());
        verify(matching).findSuitableCandidates(10L, 5);
    }

    @org.junit.jupiter.params.ParameterizedTest
    @org.junit.jupiter.params.provider.ValueSource(ints = {0, 101})
    void matchingRejectsInvalidLimitsBeforeCallingModel(int limit) throws Exception {
        when(vacancies.getVacancyById(10L)).thenReturn(vacancy(10, 2));
        mvc.perform(
                        operation(
                                        "query Q { matchingCandidates(vacancyId:\"10\",limit:"
                                                + limit
                                                + ") { id } }",
                                        "Q",
                                        Map.of())
                                .principal(actor(AccountType.EMPLOYER, 2)))
                .andExpect(jsonPath("$.errors[0].extensions.code").value("BAD_USER_INPUT"));
        verifyNoInteractions(matching);
    }
}
