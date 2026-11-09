package com.ofdun.jobfinder.features.api;

public class ApiFailure extends RuntimeException {
    private final String code;
    private final int status;

    public ApiFailure(String code, int status, String message) {
        super(message);
        this.code = code;
        this.status = status;
    }

    public String getCode() {
        return code;
    }

    public int getStatus() {
        return status;
    }
}
