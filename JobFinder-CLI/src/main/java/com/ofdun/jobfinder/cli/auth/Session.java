package com.ofdun.jobfinder.cli.auth;

import com.ofdun.jobfinder.cli.config.AppConfig;
import java.util.Objects;
import lombok.Getter;

@Getter
public final class Session {
    private final String baseUrl;
    private final UserRole role;
    private final String accessToken;
    private final String refreshToken;

    public Session(String baseUrl, UserRole role, String accessToken, String refreshToken) {
        this.baseUrl = Objects.requireNonNullElse(baseUrl, AppConfig.load().getBaseUrl());
        this.role = role == null ? UserRole.GUEST : role;
        this.accessToken = accessToken;
        this.refreshToken = refreshToken;
    }

    public static Session guest() {
        return new Session(AppConfig.load().getBaseUrl(), UserRole.GUEST, null, null);
    }

    public boolean isAuthorized() {
        return role != UserRole.GUEST && accessToken != null && !accessToken.isBlank();
    }

    public Session withBaseUrl(String baseUrl) {
        return new Session(baseUrl, role, accessToken, refreshToken);
    }

    public Session authorized(UserRole role, String accessToken, String refreshToken) {
        return new Session(baseUrl, role, accessToken, refreshToken);
    }

    public Session logout() {
        return new Session(baseUrl, UserRole.GUEST, null, null);
    }
}
