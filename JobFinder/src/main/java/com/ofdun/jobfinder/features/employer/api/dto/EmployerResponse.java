package com.ofdun.jobfinder.features.employer.api.dto;

import com.ofdun.jobfinder.features.location.api.dto.LocationDto;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class EmployerResponse {
    private Long id;
    private String name;
    private String email;
    private String description;
    private String address;
    private String websiteLink;
    private LocationDto location;
}
