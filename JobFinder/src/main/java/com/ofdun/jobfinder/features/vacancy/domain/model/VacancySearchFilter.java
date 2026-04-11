package com.ofdun.jobfinder.features.vacancy.domain.model;

import com.ofdun.jobfinder.features.vacancy.enums.EmploymentType;
import com.ofdun.jobfinder.features.vacancy.enums.JobFormat;
import com.ofdun.jobfinder.features.vacancy.enums.PaymentFrequency;
import java.math.BigDecimal;
import java.util.Date;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class VacancySearchFilter {
    private String q;
    private Long employerId;
    private Long locationId;
    private BigDecimal salaryMin;
    private BigDecimal salaryMax;
    private PaymentFrequency paymentFrequency;
    private EmploymentType employmentType;
    private JobFormat workFormat;
    private Date publicationDateFrom;
    private Date publicationDateTo;
    private List<Long> skillIds;
    private List<Long> languageIds;
}
