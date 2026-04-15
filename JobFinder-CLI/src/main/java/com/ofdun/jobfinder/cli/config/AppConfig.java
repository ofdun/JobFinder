package com.ofdun.jobfinder.cli.config;

import java.io.IOException;
import java.io.InputStream;
import java.util.Objects;
import java.util.Properties;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public final class AppConfig {
    private static final String DEFAULT_BASE_URL = "http://localhost:8080/api/v1";

    private final String baseUrl;

    public static AppConfig load() {
        Properties props = new Properties();
        try (InputStream is =
                AppConfig.class.getClassLoader().getResourceAsStream("application.properties")) {
            if (is != null) {
                props.load(is);
            }
        } catch (IOException ignored) {
        }

        String baseUrl =
                Objects.requireNonNullElse(props.getProperty("jobfinder.baseUrl"), DEFAULT_BASE_URL)
                        .trim();
        if (baseUrl.isBlank()) baseUrl = DEFAULT_BASE_URL;
        return new AppConfig(baseUrl);
    }
}
