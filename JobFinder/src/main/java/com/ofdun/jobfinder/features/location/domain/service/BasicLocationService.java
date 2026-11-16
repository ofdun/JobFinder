package com.ofdun.jobfinder.features.location.domain.service;

import com.ofdun.jobfinder.features.location.domain.model.LocationModel;
import com.ofdun.jobfinder.features.location.domain.repository.LocationRepository;
import com.ofdun.jobfinder.features.location.exception.LocationNotFoundException;
import io.micrometer.observation.annotation.Observed;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class BasicLocationService implements LocationService {
    private final LocationRepository locationRepository;

    @Override
    @Observed(
            name = "jobfinder.location.service.getLocationById",
            contextualName = "Получение местоположения по ID",
            lowCardinalityKeyValues = {"feature", "location", "layer", "service"})
    public LocationModel getLocationById(Long id) {
        return locationRepository
                .getLocationById(id)
                .orElseThrow(() -> new LocationNotFoundException(id));
    }

    @Override
    @Observed(
            name = "jobfinder.location.service.searchLocations",
            contextualName = "Поиск местоположений",
            lowCardinalityKeyValues = {"feature", "location", "layer", "service"})
    public List<LocationModel> searchLocations(String query, int limit) {
        int safeLimit = Math.min(50, Math.max(1, limit));
        return locationRepository.searchLocations(query, safeLimit);
    }
}
