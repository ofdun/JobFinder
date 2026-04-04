package com.ofdun.jobfinder.features.vacancy.domain.model;

import com.ofdun.jobfinder.shared.domain.model.LocationModel;
import com.ofdun.jobfinder.shared.vacancy.domain.enums.EmploymentType;
import com.ofdun.jobfinder.shared.vacancy.domain.enums.JobFormat;
import com.ofdun.jobfinder.shared.vacancy.domain.enums.PaymentFrequency;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.ToString;

import java.math.BigDecimal;
import java.util.Date;
import java.util.List;

@AllArgsConstructor
@Data
@ToString
public class VacancyModel {
    private Long id;

    @NotNull
    private Long employerId;

    @NotNull
    @Valid
    private LocationModel location;

    @NotNull
    private BigDecimal salary;

    private List<@NotNull String> skills;

    @NotNull
    private PaymentFrequency paymentFrequency;

    @NotBlank
    private String experience;

    @NotNull
    private JobFormat jobFormat;

    @NotNull
    private EmploymentType employmentType;

    @NotBlank
    private String description;

    @NotNull
    private Date publicationDate;

    @NotBlank
    private String address;
}
