package com.ofdun.jobfinder.features.api;

import java.util.*;
import org.springframework.core.MethodParameter;
import org.springframework.http.*;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.server.*;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.servlet.mvc.method.annotation.ResponseBodyAdvice;

@ControllerAdvice
public class ApiResponseAdvice implements ResponseBodyAdvice<Object> {
    @Override
    public boolean supports(
            MethodParameter method, Class<? extends HttpMessageConverter<?>> converter) {
        return true;
    }

    @Override
    public Object beforeBodyWrite(
            Object body,
            MethodParameter method,
            MediaType contentType,
            Class<? extends HttpMessageConverter<?>> converter,
            ServerHttpRequest request,
            ServerHttpResponse response) {
        String path = request.getURI().getPath();
        if (!path.startsWith("/api/v2/")) return body;
        if (path.contains("/auth/")) response.getHeaders().setCacheControl("no-store");
        int status =
                response instanceof ServletServerHttpResponse servlet
                        ? servlet.getServletResponse().getStatus()
                        : 200;
        if (status == 401) response.getHeaders().set("WWW-Authenticate", "Bearer");
        if (status == 201 && body != null) {
            Object id = body;
            if (body
                    instanceof
                    com.ofdun.jobfinder.features.vacancy.api.dto.DraftVacancyResponse draft)
                id = draft.getId();
            response.getHeaders().set("Location", path + "/" + id);
        }
        if (body instanceof ApiErrorResponse error)
            return new ApiErrorResponse(error.description(), error.code(), null, null, List.of());
        normalizeLists(body);
        return body;
    }

    private void normalizeLists(Object body) {
        if (body instanceof com.ofdun.jobfinder.common.api.dto.PageResponse<?> page) {
            if (page.getItems() != null) page.getItems().forEach(this::normalizeLists);
        }
        if (body instanceof Collection<?> items) items.forEach(this::normalizeLists);
        if (body instanceof com.ofdun.jobfinder.features.resume.api.dto.ResumeResponse resume) {
            if (resume.getSkills() == null) resume.setSkills(List.of());
            if (resume.getLanguages() == null) resume.setLanguages(List.of());
            if (resume.getEducations() == null) resume.setEducations(List.of());
            if (resume.getJobExperiences() == null) resume.setJobExperiences(List.of());
        }
    }
}
