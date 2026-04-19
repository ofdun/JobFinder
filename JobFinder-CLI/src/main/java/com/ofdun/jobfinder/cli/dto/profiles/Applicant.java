package com.ofdun.jobfinder.cli.dto.profiles;

import com.ofdun.jobfinder.cli.dto.common.Location;

public record Applicant(
        Long id,
        String name,
        String email,
        String address,
        String phoneNumber,
        Location location) {}
