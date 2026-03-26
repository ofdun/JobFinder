package com.ofdun.jobfinder.features.employer.domain.model;

import lombok.AllArgsConstructor;
import lombok.Data;

@AllArgsConstructor
@Data
public class EmployerModel {
    private Long id;
    private String name;
    private String description;
    private String address;
    private String siteUrl;
    private String email;
    private String city;
    private String country;
}
