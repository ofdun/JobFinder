package com.ofdun.jobfinder.features.api;

import jakarta.servlet.http.HttpServletRequest;
import java.io.*;
import java.lang.reflect.Type;
import lombok.RequiredArgsConstructor;
import org.springframework.core.MethodParameter;
import org.springframework.http.*;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.servlet.mvc.method.annotation.RequestBodyAdviceAdapter;

@ControllerAdvice
@RequiredArgsConstructor
public class ApiContractAdvice extends RequestBodyAdviceAdapter {
    private final OpenApiContract contract;
    private final HttpServletRequest request;

    @Override
    public boolean supports(
            MethodParameter parameter,
            Type type,
            Class<? extends HttpMessageConverter<?>> converter) {
        return request.getRequestURI().startsWith("/api/v2/");
    }

    @Override
    public HttpInputMessage beforeBodyRead(
            HttpInputMessage input,
            MethodParameter parameter,
            Type type,
            Class<? extends HttpMessageConverter<?>> converter)
            throws IOException {
        byte[] bytes = input.getBody().readNBytes(1048577);
        if (bytes.length > 1048576) throw new IllegalArgumentException("Request too large");
        try {
            contract.validateBody(request, bytes);
        } catch (com.fasterxml.jackson.core.JacksonException ex) {
            throw new IllegalArgumentException("Invalid JSON");
        }
        return new HttpInputMessage() {
            public InputStream getBody() {
                return new ByteArrayInputStream(bytes);
            }

            public HttpHeaders getHeaders() {
                return input.getHeaders();
            }
        };
    }
}
