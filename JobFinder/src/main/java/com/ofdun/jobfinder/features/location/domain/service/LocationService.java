package com.ofdun.jobfinder.features.location.domain.service;

import com.ofdun.jobfinder.features.location.domain.model.LocationModel;
import java.util.List;

public interface LocationService {
    LocationModel getLocationById(Long id);

    List<LocationModel> searchLocations(String query, int limit);
}
