package com.ofdun.jobfinder.features.auth.mfa;

public class AuthenticationChallenge extends RuntimeException {
    private final String challengeToken;
    private final String nextStep;

    public AuthenticationChallenge(String challengeToken, String nextStep) {
        super("Additional authentication required");
        this.challengeToken = challengeToken;
        this.nextStep = nextStep;
    }

    public String getChallengeToken() {
        return challengeToken;
    }

    public String getNextStep() {
        return nextStep;
    }
}
