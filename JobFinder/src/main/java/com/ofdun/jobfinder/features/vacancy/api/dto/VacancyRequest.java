package com.ofdun.jobfinder.features.vacancy.api.dto;

import lombok.NoArgsConstructor;
import lombok.Data;
import lombok.AllArgsConstructor;
import java.util.List;
import java.math.BigDecimal;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.NotBlank;
import com.ofdun.jobfinder.features.vacancy.enums.PaymentFrequency;
import com.ofdun.jobfinder.features.vacancy.enums.JobFormat;
import com.ofdun.jobfinder.features.vacancy.enums.EmploymentType;

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
