package com.ofdun.jobfinder.features.vacancy.data.mongo.entity;

import com.ofdun.jobfinder.features.vacancy.enums.EmploymentType;
import com.ofdun.jobfinder.features.vacancy.enums.JobFormat;
import com.ofdun.jobfinder.features.vacancy.enums.PaymentFrequency;
import com.ofdun.jobfinder.features.vacancy.enums.VacancyStatus;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.util.Date;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@Data
@Document(collection = "vacancies")
@AllArgsConstructor
@NoArgsConstructor
public class VacancyDocument {
    @Id
    private Long id;

    @NotNull private Long employerId;

    @NotNull private Long locationId;

    @NotNull private BigDecimal salary;

    private List<@NotNull @Valid Long> skillIds;

    private List<@NotNull @Valid Long> languageIds;

    @NotNull private PaymentFrequency paymentFrequency;

    @NotBlank private String workExperience;

    @NotNull private JobFormat workFormat;

    @NotNull private EmploymentType employmentType;

    @NotBlank private String description;

    @NotNull private Date publicationDate;

    @NotBlank private String address;

    @NotNull private VacancyStatus status;
}

