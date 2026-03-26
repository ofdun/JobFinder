package com.ofdun.jobfinder.features.auth.domain.service;

import com.ofdun.jobfinder.features.auth.domain.model.TokenPair;

public interface AuthService {
    TokenPair login(String email, String password);
    TokenPair refreshToken(String token);
    void logout(String refreshToken);
}
