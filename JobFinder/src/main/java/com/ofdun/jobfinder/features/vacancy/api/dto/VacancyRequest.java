package com.ofdun.jobfinder.features.vacancy.api.dto;

import com.ofdun.jobfinder.features.vacancy.enums.EmploymentType;
import com.ofdun.jobfinder.features.vacancy.enums.JobFormat;
import com.ofdun.jobfinder.features.vacancy.enums.PaymentFrequency;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@AllArgsConstructor
@Data
public class VacancyRequest {
    @NotBlank private String address;

    @NotBlank private String description;

    @NotNull private EmploymentType employmentType;

    @NotNull private JobFormat jobFormat;

    @NotBlank private String experience;

    @NotNull private PaymentFrequency paymentFrequency;

    private List<@NotNull Long> languageIds;

    private List<@NotNull Long> skillIds;

    @NotNull private BigDecimal salary;

    @NotNull private Long locationId;

    @NotNull private Long employerId;
}
