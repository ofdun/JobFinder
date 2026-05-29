package com.ofdun.jobfinder.features.location.data.repository;

import static org.junit.jupiter.api.Assertions.*;

import com.ofdun.jobfinder.common.test.MongoRepositoryITBase;
import com.ofdun.jobfinder.features.location.data.mongo.entity.LocationDocument;
import com.ofdun.jobfinder.features.location.data.mongo.repository.MongoLocationCRUDRepository;
import com.ofdun.jobfinder.features.location.data.mongo.repository.MongoLocationRepository;
import com.ofdun.jobfinder.features.location.domain.repository.LocationRepository;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;

@Tag("mongo")
@ActiveProfiles("test-mongo")
@Import(MongoLocationRepository.class)
class MongoLocationRepositoryIT extends MongoRepositoryITBase {

    @Autowired private LocationRepository locationRepository;
    @Autowired private MongoLocationCRUDRepository mongoLocationCRUDRepository;

    @Test
    void searchLocations_matchesCityOrCountry() {
        mongoLocationCRUDRepository.save(new LocationDocument(1L, "Moscow", "Russia"));
        mongoLocationCRUDRepository.save(new LocationDocument(2L, "Berlin", "Germany"));

        var result = locationRepository.searchLocations("rus", 10);

        assertEquals(1, result.size());
        assertEquals("Moscow", result.get(0).getCity());
    }
}
