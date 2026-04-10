package com.ofdun.jobfinder.features.vacancy.domain.model;

import com.ofdun.jobfinder.features.vacancy.enums.EmploymentType;
import com.ofdun.jobfinder.features.vacancy.enums.JobFormat;
import com.ofdun.jobfinder.features.vacancy.enums.PaymentFrequency;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.util.Date;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.ToString;

@AllArgsConstructor
@Data
@ToString
public class VacancyModel {
    private Long id;

    @NotNull private Long employerId;

    @NotNull private Long locationId;

    @NotNull private BigDecimal salary;

    private List<@NotNull Long> skillIds;

    private List<@NotNull Long> languageIds;

    @NotNull private PaymentFrequency paymentFrequency;

    @NotBlank private String experience;

    @NotNull private JobFormat jobFormat;

    @NotNull private EmploymentType employmentType;

    @NotBlank private String description;

    @NotNull private Date publicationDate;

    @NotBlank private String address;
}
