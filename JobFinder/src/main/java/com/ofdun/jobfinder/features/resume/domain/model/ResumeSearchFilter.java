package com.ofdun.jobfinder.features.resume.domain.model;

import java.util.Date;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ResumeSearchFilter {
    private String q;
    private Long applicantId;
    private Long categoryId;
    private Date creationDateFrom;
    private Date creationDateTo;
    private List<Long> skillIds;
    private List<Long> languageIds;
}
