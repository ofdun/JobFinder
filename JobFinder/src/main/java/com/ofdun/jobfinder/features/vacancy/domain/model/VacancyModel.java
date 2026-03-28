package com.ofdun.jobfinder.features.vacancy.domain.model;

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
    private Long employerId;
    private String city;
    private String country;
    private BigDecimal salary;
    private List<String> skills;
    private PaymentFrequency paymentFrequency;
    private String experience;
    private JobFormat jobFormat;
    private EmploymentType employmentType;
    private String description;
    private Date publicationDate;
    private String address;
}
