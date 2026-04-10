package com.ofdun.jobfinder.features.location.domain.repository;

import com.ofdun.jobfinder.features.location.domain.model.LocationModel;

import java.util.Optional;

public interface LocationRepository {
    Optional<LocationModel> getLocationById(Long id);
}
