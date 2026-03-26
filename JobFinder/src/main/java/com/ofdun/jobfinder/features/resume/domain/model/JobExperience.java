package com.ofdun.jobfinder.features.resume.domain.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.ToString;

import java.util.Date;

@AllArgsConstructor
@Data
@ToString
public class JobExperience {
    private Long resumeId;
    private String position;
    private String companyName;
    private String description;
    private Date startDate;
    private Date endDate;
}
