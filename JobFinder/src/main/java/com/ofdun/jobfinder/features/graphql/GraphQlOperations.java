package com.ofdun.jobfinder.features.graphql;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.ofdun.jobfinder.common.domain.model.OffsetPagination;
import com.ofdun.jobfinder.common.domain.model.PageResult;
import com.ofdun.jobfinder.features.api.ApiAccess;
import com.ofdun.jobfinder.features.api.ApiFailure;
import com.ofdun.jobfinder.features.applicant.api.dto.*;
import com.ofdun.jobfinder.features.applicant.api.mapper.ApplicantApiMapper;
import com.ofdun.jobfinder.features.applicant.domain.service.ApplicantService;
import com.ofdun.jobfinder.features.application.api.dto.*;
import com.ofdun.jobfinder.features.application.api.mapper.ApplicationApiMapper;
import com.ofdun.jobfinder.features.application.domain.model.ApplicationModel;
import com.ofdun.jobfinder.features.application.domain.service.ApplicationService;
import com.ofdun.jobfinder.features.application.enums.ApplicationStatus;
import com.ofdun.jobfinder.features.auth.domain.service.*;
import com.ofdun.jobfinder.features.auth.enums.AccountType;
import com.ofdun.jobfinder.features.category.domain.service.CategoryService;
import com.ofdun.jobfinder.features.employer.api.dto.*;
import com.ofdun.jobfinder.features.employer.api.mapper.EmployerApiMapper;
import com.ofdun.jobfinder.features.employer.domain.service.EmployerService;
import com.ofdun.jobfinder.features.language.domain.service.LanguageService;
import com.ofdun.jobfinder.features.location.domain.service.LocationService;
import com.ofdun.jobfinder.features.matching.domain.service.MatchingService;
import com.ofdun.jobfinder.features.resume.api.dto.*;
import com.ofdun.jobfinder.features.resume.api.mapper.ResumeApiMapper;
import com.ofdun.jobfinder.features.resume.domain.model.*;
import com.ofdun.jobfinder.features.resume.domain.service.ResumeService;
import com.ofdun.jobfinder.features.skill.domain.service.SkillService;
import com.ofdun.jobfinder.features.vacancy.api.dto.*;
import com.ofdun.jobfinder.features.vacancy.api.mapper.*;
import com.ofdun.jobfinder.features.vacancy.domain.model.*;
import com.ofdun.jobfinder.features.vacancy.domain.service.*;
import graphql.schema.DataFetchingEnvironment;
import jakarta.validation.Validator;
import java.util.*;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class GraphQlOperations {
    private final ApiAccess access;
    private final ApplicantService applicants;
    private final EmployerService employers;
    private final VacancyService vacancies;
    private final DraftVacancyService drafts;
    private final ResumeService resumes;
    private final ApplicationService applications;
    private final ApplicantAuthService applicantAuth;
    private final EmployerAuthService employerAuth;
    private final CategoryService categories;
    private final SkillService skills;
    private final LanguageService languages;
    private final LocationService locations;
    private final MatchingService matching;
    private final ApplicantApiMapper applicantMapper;
    private final EmployerApiMapper employerMapper;
    private final VacancyApiMapper vacancyMapper;
    private final ResumeApiMapper resumeMapper;
    private final ApplicationApiMapper applicationMapper;
    private final DraftVacancyApiMapper draftMapper;
    private final Validator validator;
    private final ObjectMapper json =
            new ObjectMapper()
                    .findAndRegisterModules()
                    .disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);

    public Object query(DataFetchingEnvironment env) {
        Authentication auth = env.getGraphQlContext().get("authentication");
        Map<String, Object> a = env.getArguments();
        return switch (env.getField().getName()) {
            case "applicant" -> {
                Long id = id(a, "id");
                access.self(auth, AccountType.APPLICANT, id);
                yield applicant(id);
            }
            case "employer" -> {
                Long id = id(a, "id");
                access.self(auth, AccountType.EMPLOYER, id);
                yield employer(id);
            }
            case "vacancy" -> vacancy(id(a, "id"));
            case "vacancies" -> searchVacancies(a);
            case "resume" -> resume(auth, id(a, "id"));
            case "resumes" -> searchResumes(auth, a);
            case "application" ->
                    applicationMapper.toResponse(access.application(auth, id(a, "id")));
            case "myApplications" -> {
                var p = page(a);
                var actor = access.role(auth, AccountType.APPLICANT);
                var result =
                        applications.getApplicationsByApplicantId(
                                actor.id(), p.getLimit(), p.getOffset());
                result.forEach(x -> access.application(auth, x.getId()));
                yield result.stream().map(applicationMapper::toResponse).toList();
            }
            case "vacancyApplications" -> {
                Long id = id(a, "vacancyId");
                access.ownVacancy(auth, id);
                var list =
                        applications.getApplicationsByVacancyId(id).stream()
                                .sorted(
                                        Comparator.comparing(ApplicationModel::getApplicationDate)
                                                .thenComparing(ApplicationModel::getId)
                                                .reversed())
                                .toList();
                var result = slice(list, page(a));
                result.forEach(x -> access.employerApplication(auth, x.getId()));
                yield result.stream().map(applicationMapper::toResponse).toList();
            }
            case "vacancyDrafts" -> {
                Long id = id(a, "vacancyId");
                access.ownVacancy(auth, id);
                var list =
                        drafts.getDrafts(id).stream()
                                .sorted(
                                        Comparator.comparing(DraftVacancyModel::getVersionTimestamp)
                                                .thenComparing(DraftVacancyModel::getId)
                                                .reversed())
                                .toList();
                yield slice(list, page(a)).stream().map(draftMapper::toResponse).toList();
            }
            case "matchingCandidates" -> {
                Long id = id(a, "vacancyId");
                access.ownVacancy(auth, id);
                int limit = bounded(a.getOrDefault("limit", 10), 1, 100);
                yield new com.ofdun.jobfinder.features.matching.api.mapper.MatchingApiMapper()
                        .toResponseList(matching.findSuitableCandidates(id, limit));
            }
            case "categories" ->
                    categories.getAllCategories().stream()
                            .map(
                                    com.ofdun.jobfinder.features.category.api.mapper
                                                    .CategoryApiMapper
                                            ::toDto)
                            .toList();
            case "skills" ->
                    skills.getAllSkills().stream()
                            .map(
                                    com.ofdun.jobfinder.features.skill.api.mapper.SkillApiMapper
                                            ::toDto)
                            .toList();
            case "languages" ->
                    languages.getAllLanguages().stream()
                            .map(
                                    com.ofdun.jobfinder.features.language.api.mapper
                                                    .LanguageApiMapper
                                            ::toDto)
                            .toList();
            case "location" ->
                    com.ofdun.jobfinder.features.location.api.mapper.LocationApiMapper.toDto(
                            locations.getLocationById(id(a, "id")));
            case "locations" -> {
                String q = (String) a.get("q");
                if (q.isBlank()) throw new IllegalArgumentException("Search query is required");
                yield locations
                        .searchLocations(q, bounded(a.getOrDefault("limit", 20), 1, 50))
                        .stream()
                        .map(
                                com.ofdun.jobfinder.features.location.api.mapper.LocationApiMapper
                                        ::toDto)
                        .toList();
            }
            default -> throw new IllegalStateException("Unmapped query");
        };
    }

    public Object mutation(DataFetchingEnvironment env) {
        Authentication auth = env.getGraphQlContext().get("authentication");
        Map<String, Object> a = env.getArguments();
        var input = input(a);
        return switch (env.getField().getName()) {
            case "registerApplicant" ->
                    applicant(
                            applicants.createApplicant(
                                    applicantMapper.toModel(dto(input, ApplicantRequest.class))));
            case "registerEmployer" ->
                    employer(
                            employers.createEmployer(
                                    employerMapper.toModel(dto(input, EmployerRequest.class))));
            case "login" -> {
                AccountType type = AccountType.valueOf((String) input.get("accountType"));
                var email = (String) input.get("email");
                var password = (String) input.get("password");
                if (password.isBlank()) throw new IllegalArgumentException("Password is required");
                var pair =
                        type == AccountType.APPLICANT
                                ? applicantAuth.login(email, password)
                                : employerAuth.login(email, password);
                yield Map.of(
                        "accessToken", pair.accessToken(), "refreshToken", pair.refreshToken());
            }
            case "refreshTokens" -> {
                AccountType type = AccountType.valueOf((String) input.get("accountType"));
                var token = (String) input.get("refreshToken");
                if (token.isBlank()) throw new IllegalArgumentException("Refresh token required");
                var pair =
                        type == AccountType.APPLICANT
                                ? applicantAuth.refreshToken(token)
                                : employerAuth.refreshToken(token);
                yield Map.of(
                        "accessToken", pair.accessToken(), "refreshToken", pair.refreshToken());
            }
            case "replaceApplicant" -> {
                Long id = id(a, "id");
                access.self(auth, AccountType.APPLICANT, id);
                var existing = applicants.getApplicantById(id);
                var model =
                        applicantMapper.toModel(
                                dto(input, ApplicantUpdateRequest.class),
                                existing.getPasswordHash());
                model.setId(id);
                applicants.updateApplicant(model);
                yield applicant(id);
            }
            case "replaceEmployer" -> {
                Long id = id(a, "id");
                access.self(auth, AccountType.EMPLOYER, id);
                var existing = employers.getEmployerById(id);
                var model =
                        employerMapper.toModel(
                                dto(input, EmployerUpdateRequest.class),
                                existing.getPasswordHash());
                model.setId(id);
                employers.updateEmployer(model);
                yield employer(id);
            }
            case "deleteApplicant" -> {
                Long id = id(a, "id");
                access.self(auth, AccountType.APPLICANT, id);
                applicants.deleteApplicant(id);
                yield deleted(id);
            }
            case "deleteEmployer" -> {
                Long id = id(a, "id");
                access.self(auth, AccountType.EMPLOYER, id);
                employers.deleteEmployer(id);
                yield deleted(id);
            }
            case "createVacancy" -> {
                var request = dto(input, VacancyRequest.class);
                access.self(auth, AccountType.EMPLOYER, request.getEmployerId());
                yield vacancy(vacancies.createVacancy(vacancyMapper.toModel(request)));
            }
            case "replaceVacancy" -> {
                Long id = id(a, "id");
                var old = access.ownVacancy(auth, id);
                var request = dto(input, VacancyRequest.class);
                ApiAccess.require(old.getEmployerId().equals(request.getEmployerId()));
                var model = vacancyMapper.toModel(request);
                model.setId(id);
                model.setPublicationDate(old.getPublicationDate());
                vacancies.updateVacancy(model);
                yield vacancy(id);
            }
            case "deleteVacancy" -> {
                Long id = id(a, "id");
                access.ownVacancy(auth, id);
                vacancies.deleteVacancy(id);
                yield deleted(id);
            }
            case "createVacancyDraft" -> {
                Long id = id(a, "vacancyId");
                access.ownVacancy(auth, id);
                yield draftMapper.toResponse(drafts.createDraft(id));
            }
            case "restoreVacancyDraft" -> {
                Long id = id(a, "vacancyId");
                access.ownVacancy(auth, id);
                drafts.applyDraft(id, id(a, "draftId"));
                yield vacancy(id);
            }
            case "deleteVacancyDraft" -> {
                Long id = id(a, "vacancyId");
                access.ownVacancy(auth, id);
                Long draftId = id(a, "draftId");
                drafts.deleteDraft(id, draftId);
                yield deleted(draftId);
            }
            case "createResume" -> {
                var request = dto(input, ResumeRequest.class);
                access.self(auth, AccountType.APPLICANT, request.getApplicantId());
                yield resume(auth, resumes.createResume(resumeMapper.toModel(request)));
            }
            case "replaceResume" -> {
                Long id = id(a, "id");
                var old = access.ownResume(auth, id);
                var request = dto(input, ResumeUpdateRequest.class);
                ApiAccess.require(old.getApplicantId().equals(request.getApplicantId()));
                var model = resumeMapper.toModel(request);
                model.setId(id);
                resumes.replaceResume(model)
                        .orElseThrow(() -> new ApiFailure("NOT_FOUND", 404, "Resume not found"));
                yield resume(auth, id);
            }
            case "deleteResume" -> {
                Long id = id(a, "id");
                access.ownResume(auth, id);
                resumes.deleteResume(id);
                yield deleted(id);
            }
            case "createApplication" -> {
                Long vacancyId = id(input, "vacancyId"), resumeId = id(input, "resumeId");
                access.newApplication(auth, vacancyId, resumeId);
                Long id =
                        applications.saveApplication(
                                new ApplicationModel(
                                        null,
                                        vacancyId,
                                        resumeId,
                                        new Date(),
                                        ApplicationStatus.NEW));
                yield applicationMapper.toResponse(applications.getApplication(id));
            }
            case "replaceApplication" -> {
                Long id = id(a, "id");
                var old = access.employerApplication(auth, id);
                if (!old.getVacancyId().equals(id(input, "vacancyId"))
                        || !old.getResumeId().equals(id(input, "resumeId")))
                    throw new ApiFailure(
                            "CONFLICT", 409, "Application relationships cannot change");
                old.setApplicationStatus(
                        ApplicationStatus.valueOf((String) input.get("applicationStatus")));
                yield applicationMapper.toResponse(applications.updateApplication(old));
            }
            case "updateApplicationStatus" -> {
                var old = access.employerApplication(auth, id(a, "id"));
                old.setApplicationStatus(ApplicationStatus.valueOf((String) a.get("status")));
                yield applicationMapper.toResponse(applications.updateApplication(old));
            }
            case "deleteApplication" -> {
                Long id = id(a, "id");
                access.applicantApplication(auth, id);
                applications.deleteApplication(id);
                yield deleted(id);
            }
            default -> throw new IllegalStateException("Unmapped mutation");
        };
    }

    public Object related(DataFetchingEnvironment env) {
        Authentication auth = env.getGraphQlContext().get("authentication");
        Map<String, Object> source = json.convertValue(env.getSource(), Map.class);
        return switch (((graphql.schema.GraphQLNamedType) env.getParentType()).getName()
                + "."
                + env.getField().getName()) {
            case "Vacancy.employer" -> {
                var e = employers.getEmployerById(id(source, "employerId"));
                var summary = new LinkedHashMap<String, Object>();
                summary.put("id", e.getId());
                summary.put("name", e.getName());
                summary.put("description", e.getDescription());
                summary.put("websiteLink", e.getWebsiteLink());
                yield summary;
            }
            case "Application.vacancy" -> {
                access.application(auth, id(source, "id"));
                yield vacancy(id(source, "vacancyId"));
            }
            case "Application.resume" -> {
                access.application(auth, id(source, "id"));
                yield resume(auth, id(source, "resumeId"));
            }
            case "MatchResult.resume" -> {
                access.role(auth, AccountType.EMPLOYER);
                yield resume(auth, id(source, "id"));
            }
            default -> throw new IllegalStateException("Unmapped relation");
        };
    }

    private Object applicant(Long id) {
        var a = applicants.getApplicantById(id);
        return applicantMapper.toResponse(a, locations.getLocationById(a.getLocationId()));
    }

    private Object employer(Long id) {
        var e = employers.getEmployerById(id);
        return employerMapper.toResponse(e, locations.getLocationById(e.getLocationId()));
    }

    private Object vacancy(Long id) {
        return vacancy(vacancies.getVacancyById(id));
    }

    private Object vacancy(VacancyModel v) {
        return vacancyMapper.toResponse(
                v,
                locations.getLocationById(v.getLocationId()),
                list(v.getSkillIds()).stream().map(skills::getSkillById).toList(),
                list(v.getLanguageIds()).stream().map(languages::getLanguageById).toList());
    }

    private Object resume(Authentication auth, Long id) {
        access.readResume(auth, id);
        var r =
                resumes.getResumeById(id)
                        .orElseThrow(() -> new ApiFailure("NOT_FOUND", 404, "Resume not found"));
        return resume(r);
    }

    private Object resume(ResumeModel r) {
        var dto =
                resumeMapper.toResponse(
                        r,
                        categories.getCategoryById(r.getCategoryId()),
                        list(r.getSkillIds()).stream().map(skills::getSkillById).toList(),
                        list(r.getLanguageIds()).stream().map(languages::getLanguageById).toList());
        Map<String, Object> result = json.convertValue(dto, Map.class);
        for (String field : List.of("skills", "languages", "educations", "jobExperiences"))
            if (result.get(field) == null) result.put(field, List.of());
        return result;
    }

    private Object searchVacancies(Map<String, Object> a) {
        var f = dto(filter(a), VacancySearchFilter.class);
        if (f.getSalaryMin() != null
                && f.getSalaryMax() != null
                && f.getSalaryMin().compareTo(f.getSalaryMax()) > 0)
            throw new IllegalArgumentException("Invalid salary range");
        dates(f.getPublicationDateFrom(), f.getPublicationDateTo());
        var p = page(a);
        p.setSortBy(
                switch (String.valueOf(a.getOrDefault("sort", "PUBLICATION_DATE"))) {
                    case "SALARY" -> "salary";
                    case "ID" -> "id";
                    default -> "publicationDate";
                });
        p.setSortDesc("DESC".equals(a.get("direction")));
        var result = vacancies.searchVacancies(f, p);
        return pageResult(result, result.getItems().stream().map(this::vacancy).toList());
    }

    private Object searchResumes(Authentication auth, Map<String, Object> a) {
        var actor = access.principal(auth);
        var f = dto(filter(a), ResumeSearchFilter.class);
        dates(f.getCreationDateFrom(), f.getCreationDateTo());
        if (actor.accountType() == AccountType.APPLICANT) {
            ApiAccess.require(f.getApplicantId() == null || f.getApplicantId().equals(actor.id()));
            f.setApplicantId(actor.id());
        }
        var p = page(a);
        p.setSortBy("ID".equals(a.get("sort")) ? "id" : "date");
        p.setSortDesc("DESC".equals(a.get("direction")));
        var result = resumes.searchResumes(f, p);
        var items =
                result.getItems().stream()
                        .map(
                                x -> {
                                    access.readResume(auth, x.getId());
                                    return resume(auth, x.getId());
                                })
                        .toList();
        return pageResult(result, items);
    }

    private static Object pageResult(PageResult<?> page, List<?> items) {
        return Map.of(
                "items",
                items,
                "page",
                page.getPage(),
                "size",
                page.getSize(),
                "totalElements",
                page.getTotalElements(),
                "totalPages",
                page.getTotalPages());
    }

    private <T> T dto(Map<String, Object> value, Class<T> type) {
        validateIds(value);
        T result = json.convertValue(value, type);
        if (!validator.validate(result).isEmpty())
            throw new IllegalArgumentException("Invalid input fields");
        return result;
    }

    private void validateIds(Map<String, Object> value) {
        value.forEach(
                (key, v) -> {
                    if (v == null) return;
                    if (key.endsWith("Id")) ApiAccess.id(v);
                    if (key.endsWith("Ids") && v instanceof List<?> l) l.forEach(ApiAccess::id);
                    if (key.equals("salary") && new java.math.BigDecimal(v.toString()).signum() < 0)
                        throw new IllegalArgumentException("Salary must be non-negative");
                });
    }

    private static Map<String, Object> deleted(Long id) {
        return Map.of("id", id);
    }

    private static Long id(Map<String, Object> values, String name) {
        return ApiAccess.id(values.get(name));
    }

    @SuppressWarnings("unchecked")
    private static Map<String, Object> input(Map<String, Object> a) {
        return (Map<String, Object>) a.getOrDefault("input", Map.of());
    }

    @SuppressWarnings("unchecked")
    private static Map<String, Object> filter(Map<String, Object> a) {
        return a.get("filter") == null ? Map.of() : (Map<String, Object>) a.get("filter");
    }

    @SuppressWarnings("unchecked")
    private static OffsetPagination page(Map<String, Object> a) {
        Map<String, Object> p =
                a.get("page") == null ? Map.of() : (Map<String, Object>) a.get("page");
        return OffsetPagination.builder()
                .limit(bounded(p.getOrDefault("limit", 20), 1, 100))
                .offset(bounded(p.getOrDefault("offset", 0), 0, Integer.MAX_VALUE))
                .build();
    }

    private static int bounded(Object value, int min, int max) {
        if (!(value instanceof Number n) || n.longValue() < min || n.longValue() > max)
            throw new IllegalArgumentException("Value outside allowed range");
        return n.intValue();
    }

    private static void dates(Date from, Date to) {
        if (from != null && to != null && from.after(to))
            throw new IllegalArgumentException("Invalid date range");
    }

    private static <T> List<T> list(List<T> value) {
        return value == null ? List.of() : value;
    }

    private static <T> List<T> slice(List<T> list, OffsetPagination p) {
        return list.stream().skip(p.getOffset()).limit(p.getLimit()).toList();
    }
}
