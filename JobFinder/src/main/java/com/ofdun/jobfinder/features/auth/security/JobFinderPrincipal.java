package com.ofdun.jobfinder.features.auth.security;

import com.ofdun.jobfinder.features.auth.enums.AccountType;

public record JobFinderPrincipal(Long id, AccountType accountType) {}

