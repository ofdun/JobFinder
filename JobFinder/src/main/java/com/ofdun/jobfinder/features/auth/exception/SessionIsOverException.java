package com.ofdun.jobfinder.features.auth.exception;

public class SessionIsOverException extends RuntimeException {
    public SessionIsOverException() {
        super("Session is over");
    }
}
