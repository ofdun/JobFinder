package com.ofdun.jobfinder.features.auth.mfa;

import java.util.ArrayList;
import java.util.List;

public class SecurityState {
    public String secret;
    public String pendingSecret;
    public long pendingUntil;
    public long lastStep = -1;
    public long passwordChangedAt = System.currentTimeMillis();
    public long sessionVersion;
    public int failures;
    public boolean locked;
    public long lockedUntil;
    public List<String> recoveryHashes = new ArrayList<>();
}
