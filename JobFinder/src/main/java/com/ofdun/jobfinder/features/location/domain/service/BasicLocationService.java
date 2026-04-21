package com.ofdun.jobfinder.features.location.domain.service;

import com.ofdun.jobfinder.features.location.domain.model.LocationModel;
import com.ofdun.jobfinder.features.location.domain.repository.LocationRepository;
import java.util.List;
import com.ofdun.jobfinder.features.location.exception.LocationNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class BasicLocationService implements LocationService {
    private final LocationRepository locationRepository;

    @Override
    public LocationModel getLocationById(Long id) {
        return locationRepository
                .getLocationById(id)
                .orElseThrow(() -> new LocationNotFoundException(id));
    }

    @Override
    public List<LocationModel> searchLocations(String query, int limit) {
        int safeLimit = Math.min(50, Math.max(1, limit));
        return locationRepository.searchLocations(query, safeLimit);
    }
}
