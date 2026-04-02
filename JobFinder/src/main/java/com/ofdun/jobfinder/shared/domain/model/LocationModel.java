package com.ofdun.jobfinder.shared.domain.model;

import lombok.AllArgsConstructor;
import lombok.Data;

@AllArgsConstructor
@Data
public class LocationModel {
    private Long id;
    private String city;
    private String country;
}
