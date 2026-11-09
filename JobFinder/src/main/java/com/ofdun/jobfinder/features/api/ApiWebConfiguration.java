package com.ofdun.jobfinder.features.api;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.*;

@Configuration
@RequiredArgsConstructor
public class ApiWebConfiguration implements WebMvcConfigurer {
    private final OpenApiContract contract;

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(contract).addPathPatterns("/api/v2/**");
    }
}
