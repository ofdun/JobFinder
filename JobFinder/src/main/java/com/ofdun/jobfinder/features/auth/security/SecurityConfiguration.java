package com.ofdun.jobfinder.features.auth.security;

import com.ofdun.jobfinder.common.logging.UserActionLoggingFilter;
import com.ofdun.jobfinder.features.auth.domain.jwt.JwtProvider;
import java.util.List;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

@Configuration
@EnableMethodSecurity
public class SecurityConfiguration {

    @Bean
    public SecurityFilterChain securityFilterChain(
            HttpSecurity http,
            JwtProvider jwtProvider,
            UserActionLoggingFilter userActionLoggingFilter) {
        return http.csrf(csrf -> csrf.disable())
                .cors(cors -> {})
                .sessionManagement(
                        session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(
                        auth ->
                                auth.requestMatchers(HttpMethod.OPTIONS, "/**")
                                        .permitAll()
                                        .requestMatchers(
                                                "/api/v2",
                                                "/api/v2/docs",
                                                "/api/v2/openapi.yml",
                                                "/swagger-ui/**",
                                                "/api/v2/swagger-ui/**",
                                                "/v3/api-docs/**")
                                        .permitAll()
                                        .requestMatchers("/error")
                                        .permitAll()
                                        .requestMatchers("/api/v1/auth/**", "/api/v2/auth/**")
                                        .permitAll()
                                        .requestMatchers(
                                                HttpMethod.POST,
                                                "/api/v1/applicants",
                                                "/api/v2/applicants")
                                        .permitAll()
                                        .requestMatchers(
                                                HttpMethod.POST,
                                                "/api/v1/employers",
                                                "/api/v2/employers")
                                        .permitAll()
                                        .requestMatchers(
                                                HttpMethod.GET,
                                                "/api/v1/categories",
                                                "/api/v2/categories")
                                        .permitAll()
                                        .requestMatchers(
                                                HttpMethod.GET, "/api/v1/skills", "/api/v2/skills")
                                        .permitAll()
                                        .requestMatchers(
                                                HttpMethod.GET,
                                                "/api/v1/languages",
                                                "/api/v2/languages")
                                        .permitAll()
                                        .requestMatchers(
                                                HttpMethod.GET,
                                                "/api/v1/locations/**",
                                                "/api/v2/locations/**")
                                        .permitAll()
                                        .requestMatchers(
                                                HttpMethod.GET,
                                                "/api/v1/vacancies",
                                                "/api/v2/vacancies",
                                                "/api/v1/vacancies/*",
                                                "/api/v2/vacancies/*",
                                                "/api/v1/resumes/*")
                                        .permitAll()
                                        .anyRequest()
                                        .authenticated())
                .exceptionHandling(
                        errors ->
                                errors.authenticationEntryPoint(
                                                (req, res, ex) -> {
                                                    if (req.getRequestURI()
                                                            .startsWith("/api/v2/")) {
                                                        res.setStatus(401);
                                                        res.setHeader("WWW-Authenticate", "Bearer");
                                                        res.setContentType("application/json");
                                                        res.getWriter()
                                                                .write(
                                                                        "{\"description\":\"Authentication"
                                                                            + " required\",\"code\":401,\"exceptionName\":null,\"exceptionMessage\":null,\"stackTrace\":[]}");
                                                    } else res.sendError(403);
                                                })
                                        .accessDeniedHandler(
                                                (req, res, ex) -> {
                                                    res.setStatus(403);
                                                    res.setContentType("application/json");
                                                    res.getWriter()
                                                            .write(
                                                                    "{\"description\":\"Access"
                                                                        + " denied\",\"code\":403,\"exceptionName\":null,\"exceptionMessage\":null,\"stackTrace\":[]}");
                                                }))
                .addFilterBefore(
                        new JwtAuthenticationFilter(jwtProvider),
                        UsernamePasswordAuthenticationFilter.class)
                .addFilterAfter(userActionLoggingFilter, JwtAuthenticationFilter.class)
                .build();
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        configuration.setAllowedOrigins(List.of("http://localhost:5173"));
        configuration.setAllowedMethods(
                List.of("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS"));
        configuration.setAllowedHeaders(
                List.of("Authorization", "Content-Type", "Accept", "Origin"));
        configuration.setAllowCredentials(true);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/api/v1/**", configuration);
        source.registerCorsConfiguration("/api/v2/**", configuration);
        source.registerCorsConfiguration("/api/v2", configuration);
        return source;
    }
}
