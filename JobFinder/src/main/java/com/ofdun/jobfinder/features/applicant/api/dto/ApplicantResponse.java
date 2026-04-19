package com.ofdun.jobfinder.features.applicant.api.dto;

import com.ofdun.jobfinder.features.location.api.dto.LocationDto;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ApplicantResponse {
    private Long id;
    private String name;
    private String email;
    private String address;
    private String phoneNumber;
    private LocationDto location;
}
