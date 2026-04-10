package com.ofdun.jobfinder.features.location.data.repository;

import com.ofdun.jobfinder.features.location.data.mapper.LocationMapper;
import com.ofdun.jobfinder.features.location.domain.model.LocationModel;
import com.ofdun.jobfinder.features.location.domain.repository.LocationRepository;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
@RequiredArgsConstructor
public class PostgreSQLLocationRepository implements LocationRepository {
    private final LocationJpaRepository locationJpaRepository;

    @Override
    public Optional<LocationModel> getLocationById(Long id) {
        return locationJpaRepository.findById(id)
                .map(LocationMapper::toModel);
    }
}
