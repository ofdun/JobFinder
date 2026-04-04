package com.ofdun.jobfinder.features.clients.ai.exception;

public class AiEmptyRespondException extends RuntimeException {
    public AiEmptyRespondException() {
        super("AI response is empty");
    }
}
