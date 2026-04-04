package com.ofdun.jobfinder.features.auth.exception;

public class InvalidPasswordException extends RuntimeException {
    public InvalidPasswordException() {
        super("Password is invalid");
    }
}
