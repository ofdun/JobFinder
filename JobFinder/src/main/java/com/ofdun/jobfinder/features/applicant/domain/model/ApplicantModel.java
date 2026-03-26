package com.ofdun.jobfinder.features.applicant.domain.model;

import lombok.AllArgsConstructor;
import lombok.Data;

@AllArgsConstructor
@Data
public class ApplicantModel {
    private Long id;
    private String name;
    private String email;
    private String address;
    private String phoneNumber;
    private String city;
    private String country;
}
