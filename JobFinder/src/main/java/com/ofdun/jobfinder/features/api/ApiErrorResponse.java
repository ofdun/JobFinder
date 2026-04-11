package com.ofdun.jobfinder.features.api;

import java.util.List;

public record ApiErrorResponse(
        String description,
        Integer code,
        String exceptionName,
        String exceptionMessage,
        List<String> stackTrace) {}
