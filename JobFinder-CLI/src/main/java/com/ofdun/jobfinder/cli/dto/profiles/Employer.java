package com.ofdun.jobfinder.cli.dto.profiles;

import com.ofdun.jobfinder.cli.dto.common.Location;

public record Employer(
        Long id,
        String name,
        String email,
        String description,
        String address,
        String websiteLink,
        Location location) {}
