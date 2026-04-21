package com.ofdun.jobfinder.features.location.domain.repository;

import com.ofdun.jobfinder.features.location.domain.model.LocationModel;
import java.util.List;
import java.util.Optional;

public interface LocationRepository {
    Optional<LocationModel> getLocationById(Long id);

    List<LocationModel> searchLocations(String query, int limit);
}
