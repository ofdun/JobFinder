package com.ofdun.jobfinder.cli.dto.profiles;

public record ApplicantRequest(
        String name,
        String email,
        String password,
        String address,
        String phoneNumber,
        Long locationId) {}
