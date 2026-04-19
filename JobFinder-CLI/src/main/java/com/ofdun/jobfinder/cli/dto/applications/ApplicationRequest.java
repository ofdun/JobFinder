package com.ofdun.jobfinder.cli.dto.applications;

public record ApplicationRequest(
        Long vacancyId, Long resumeId, ApplicationStatus applicationStatus) {}
