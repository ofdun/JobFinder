package com.ofdun.jobfinder.features.application.api.dto;

import com.ofdun.jobfinder.features.application.enums.ApplicationStatus;
import java.util.Date;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class ApplicationResponse {
    private Long id;
    private Long vacancyId;
    private Long resumeId;
    private Date applicationDate;
    private ApplicationStatus applicationStatus;
}
