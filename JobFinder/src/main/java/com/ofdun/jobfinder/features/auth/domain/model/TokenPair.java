package com.ofdun.jobfinder.features.auth.domain.model;

import lombok.NonNull;

public record TokenPair(@NonNull String accessToken,
                        @NonNull String refreshToken) {
}
