package com.ofdun.jobfinder.features.api;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import com.ofdun.jobfinder.features.category.api.controller.CategoryController;
import com.ofdun.jobfinder.features.category.domain.model.CategoryModel;
import com.ofdun.jobfinder.features.category.domain.service.CategoryService;
import java.nio.charset.StandardCharsets;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.servlet.HandlerMapping;

class ApiContractTest {
    private void validate(String method, String path, String body) throws Exception {
        var request = new MockHttpServletRequest(method, path);
        request.setAttribute(HandlerMapping.BEST_MATCHING_PATTERN_ATTRIBUTE, path);
        new OpenApiContract().validateBody(request, body.getBytes(StandardCharsets.UTF_8));
    }

    @Test
    void newApplicationSchemaRequiresNewStatusAndRejectsUnknownProperties() throws Exception {
        validate(
                "POST",
                "/api/v2/applications",
                "{\"vacancyId\":1,\"resumeId\":2,\"applicationStatus\":\"NEW\"}");
        for (String body :
                List.of(
                        "{\"vacancyId\":1,\"resumeId\":2,\"applicationStatus\":\"INVITATION\"}",
                        "{\"vacancyId\":1,\"resumeId\":2,\"applicationStatus\":\"NEW\",\"owner\":4}",
                        "{\"vacancyId\":0,\"resumeId\":2,\"applicationStatus\":\"NEW\"}"))
            assertThrows(
                    IllegalArgumentException.class,
                    () -> validate("POST", "/api/v2/applications", body));
    }

    @Test
    void resumePutRequiresAllReplacementFieldsWhilePostAllowsOptionalLists() throws Exception {
        validate("POST", "/api/v2/resumes", "{\"applicantId\":1,\"categoryId\":1}");
        assertThrows(
                IllegalArgumentException.class,
                () ->
                        validate(
                                "PUT",
                                "/api/v2/resumes/{id}",
                                "{\"applicantId\":1,\"categoryId\":1}"));
        validate(
                "PUT",
                "/api/v2/resumes/{id}",
                "{\"applicantId\":1,\"categoryId\":1,\"description\":\"\",\"skillIds\":[],\"languageIds\":[],\"educations\":[],\"jobExperiences\":[]}");
    }

    @Test
    void nestedDatesAndRequiredFieldsAreValidated() throws Exception {
        String start =
                "{\"applicantId\":1,\"categoryId\":1,\"jobExperiences\":[{\"position\":\"Engineer\",\"companyName\":\"Example\",\"startDate\":\"";
        validate("POST", "/api/v2/resumes", start + "2026-02-28\",\"endDate\":null}]}");
        assertThrows(
                IllegalArgumentException.class,
                () -> validate("POST", "/api/v2/resumes", start + "2026-02-30\"}]}"));
        assertThrows(
                IllegalArgumentException.class,
                () ->
                        validate(
                                "POST",
                                "/api/v2/resumes",
                                "{\"applicantId\":1,\"categoryId\":1,\"educations\":[{}]}"));
    }

    @Test
    void v1AndV2ShareTheExistingCatalogueWithoutRemovingLegacyRoute() throws Exception {
        var service = mock(CategoryService.class);
        when(service.getAllCategories()).thenReturn(List.of(new CategoryModel(1L, "Engineering")));
        var mvc = MockMvcBuilders.standaloneSetup(new CategoryController(service)).build();
        for (String version : List.of("v1", "v2"))
            mvc.perform(get("/api/" + version + "/categories"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$[0].id").value(1));
    }

    @Test
    void pagedResumeResponsesUseEmptyCollectionsInsteadOfNull() {
        var resume = new com.ofdun.jobfinder.features.resume.api.dto.ResumeResponse();
        var page =
                new com.ofdun.jobfinder.common.api.dto.PageResponse<>(
                        List.of(resume), 0, 20, 1L, 1);
        var request =
                new org.springframework.http.server.ServletServerHttpRequest(
                        new MockHttpServletRequest("GET", "/api/v2/resumes"));
        var response =
                new org.springframework.http.server.ServletServerHttpResponse(
                        new org.springframework.mock.web.MockHttpServletResponse());
        new ApiResponseAdvice()
                .beforeBodyWrite(
                        page,
                        null,
                        org.springframework.http.MediaType.APPLICATION_JSON,
                        null,
                        request,
                        response);
        assertEquals(List.of(), resume.getEducations());
        assertEquals(List.of(), resume.getJobExperiences());
        assertEquals(List.of(), resume.getSkills());
        assertEquals(List.of(), resume.getLanguages());
    }
}
