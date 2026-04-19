package com.ofdun.jobfinder.cli.dto.applications;

import java.time.OffsetDateTime;

public record Application(
        Long id,
        Long vacancyId,
        Long resumeId,
        OffsetDateTime applicationDate,
        ApplicationStatus applicationStatus) {}
