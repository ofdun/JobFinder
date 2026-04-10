package com.ofdun.jobfinder.features.vacancy.api.dto;

import com.ofdun.jobfinder.features.language.api.dto.LanguageDto;
import com.ofdun.jobfinder.features.location.api.dto.LocationDto;
import com.ofdun.jobfinder.features.skill.api.dto.SkillDto;
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
public class VacancyResponse {
    private Long id;

    private Long employerId;

    private LocationDto location;

    private BigDecimal salary;

    private List<SkillDto> skills;

    private List<LanguageDto> languages;

    private PaymentFrequency paymentFrequency;

    private String experience;

    private JobFormat jobFormat;

    private EmploymentType employmentType;

    private String description;

    private Date publicationDate;

    private String address;
}

