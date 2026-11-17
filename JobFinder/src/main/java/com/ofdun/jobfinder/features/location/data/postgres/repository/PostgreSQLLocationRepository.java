package com.ofdun.jobfinder.features.location.data.postgres.repository;

import com.ofdun.jobfinder.features.location.data.postgres.mapper.LocationMapper;
import com.ofdun.jobfinder.features.location.domain.model.LocationModel;
import com.ofdun.jobfinder.features.location.domain.repository.LocationRepository;
import io.micrometer.observation.annotation.Observed;
import java.util.List;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@ConditionalOnProperty(name = "app.persistence.type", havingValue = "postgres")
public class PostgreSQLLocationRepository implements LocationRepository {
    private final LocationJpaRepository locationJpaRepository;

    @Override
    @Observed(
            name = "jobfinder.location.repository.getLocationById",
            contextualName = "PostgreSQL: получение местоположения по ID",
            lowCardinalityKeyValues = {
                "feature",
                "location",
                "layer",
                "repository",
                "storage",
                "PostgreSQL"
            })
    public Optional<LocationModel> getLocationById(Long id) {
        return locationJpaRepository.findById(id).map(LocationMapper::toModel);
    }

    @Override
    @Observed(
            name = "jobfinder.location.repository.searchLocations",
            contextualName = "PostgreSQL: поиск местоположений",
            lowCardinalityKeyValues = {
                "feature",
                "location",
                "layer",
                "repository",
                "storage",
                "PostgreSQL"
            })
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
