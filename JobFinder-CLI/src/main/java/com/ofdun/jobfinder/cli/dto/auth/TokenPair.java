package com.ofdun.jobfinder.cli.dto.auth;

public record TokenPair(
        String accessToken, String refreshToken, String challengeToken, String nextStep) {
    public TokenPair(String accessToken, String refreshToken) {
        this(accessToken, refreshToken, null, null);
    }
}
