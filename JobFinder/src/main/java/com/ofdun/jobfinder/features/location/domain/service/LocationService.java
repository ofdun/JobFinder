package com.ofdun.jobfinder.features.location.domain.service;

import com.ofdun.jobfinder.features.location.domain.model.LocationModel;

public interface LocationService {
    LocationModel getLocationById(Long id);
}
