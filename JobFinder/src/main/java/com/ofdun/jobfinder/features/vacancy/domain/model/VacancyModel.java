package com.ofdun.jobfinder.features.vacancy.domain.model;

import com.ofdun.jobfinder.shared.language.model.LanguageModel;
import com.ofdun.jobfinder.shared.location.model.LocationModel;
import com.ofdun.jobfinder.shared.skill.model.SkillModel;
import com.ofdun.jobfinder.shared.vacancy.enums.EmploymentType;
import com.ofdun.jobfinder.shared.vacancy.enums.JobFormat;
import com.ofdun.jobfinder.shared.vacancy.enums.PaymentFrequency;
import jakarta.validation.Valid;
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

    @NotNull @Valid private LocationModel location;

    @NotNull private BigDecimal salary;

    private List<@NotNull @Valid SkillModel> skills;

    private List<@NotNull @Valid LanguageModel> languages;

    @NotNull private PaymentFrequency paymentFrequency;

    @NotBlank private String experience;

    @NotNull private JobFormat jobFormat;

    @NotNull private EmploymentType employmentType;

    @NotBlank private String description;

    @NotNull private Date publicationDate;

    @NotBlank private String address;
}
