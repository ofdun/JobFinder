package com.ofdun.jobfinder.features.application.domain.model;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.Date;

@AllArgsConstructor
@Data
public class ApplicationModel {
    private Long id;
    private Long vacancyId;
    private Long resumeId;
    private Date applicationDate;
    private ApplicationStatus applicationStatus;
}
