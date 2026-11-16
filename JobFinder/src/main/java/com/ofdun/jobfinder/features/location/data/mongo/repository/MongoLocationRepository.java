package com.ofdun.jobfinder.features.location.data.mongo.repository;

import com.ofdun.jobfinder.features.location.data.mongo.mapper.LocationMongoMapper;
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
@ConditionalOnProperty(name = "app.persistence.type", havingValue = "mongo")
public class MongoLocationRepository implements LocationRepository {
    private final MongoLocationCRUDRepository mongoRepository;

    @Override
    @Observed(
            name = "jobfinder.location.repository.getLocationById",
            contextualName = "MongoDB: получение местоположения по ID",
            lowCardinalityKeyValues = {
                "feature",
                "location",
                "layer",
                "repository",
                "storage",
                "MongoDB"
            })
    public Optional<LocationModel> getLocationById(Long id) {
        return mongoRepository.findById(id).map(LocationMongoMapper::toModel);
    }

    @Override
    @Observed(
            name = "jobfinder.location.repository.searchLocations",
            contextualName = "MongoDB: поиск местоположений",
            lowCardinalityKeyValues = {
                "feature",
                "location",
                "layer",
                "repository",
                "storage",
                "MongoDB"
            })
    public List<LocationModel> searchLocations(String query, int limit) {
        if (query == null || query.isBlank()) {
            return List.of();
        }

        return mongoRepository
                .findByCityContainingIgnoreCaseOrCountryContainingIgnoreCaseOrderByCityAsc(
                        query.trim(), query.trim(), PageRequest.of(0, limit))
                .stream()
                .map(LocationMongoMapper::toModel)
                .toList();
    }
}
