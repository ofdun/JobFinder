package com.ofdun.jobfinder.cli.dto.profiles;

public record EmployerRequest(
        String name,
        String email,
        String password,
        String description,
        String address,
        String websiteLink,
        Long locationId) {}
