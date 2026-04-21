package com.ofdun.jobfinder.features.location.data.repository;

import com.ofdun.jobfinder.features.location.data.mapper.LocationMapper;
import com.ofdun.jobfinder.features.location.domain.model.LocationModel;
import com.ofdun.jobfinder.features.location.domain.repository.LocationRepository;
import java.util.List;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class PostgreSQLLocationRepository implements LocationRepository {
    private final LocationJpaRepository locationJpaRepository;

    @Override
    public Optional<LocationModel> getLocationById(Long id) {
        return locationJpaRepository.findById(id).map(LocationMapper::toModel);
    }

    @Override
    public List<LocationModel> searchLocations(String query, int limit) {
        if (query == null || query.isBlank()) {
            return List.of();
        }

        return locationJpaRepository
                .findByCityContainingIgnoreCaseOrCountryContainingIgnoreCaseOrderByCityAsc(
                        query.trim(), query.trim(), PageRequest.of(0, limit))
                .stream()
                .map(LocationMapper::toModel)
                .toList();
    }
}
