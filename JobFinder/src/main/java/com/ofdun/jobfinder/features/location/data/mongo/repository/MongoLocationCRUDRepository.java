package com.ofdun.jobfinder.features.location.data.mongo.repository;

import com.ofdun.jobfinder.features.location.data.mongo.entity.LocationDocument;
import java.util.List;
import lombok.NonNull;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface MongoLocationCRUDRepository
        extends MongoRepository<@NonNull LocationDocument, @NonNull Long> {
    List<LocationDocument> findByCityContainingIgnoreCaseOrCountryContainingIgnoreCaseOrderByCityAsc(
            String cityQuery, String countryQuery, Pageable pageable);
}

