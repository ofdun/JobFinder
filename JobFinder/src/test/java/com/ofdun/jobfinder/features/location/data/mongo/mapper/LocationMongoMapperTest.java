package com.ofdun.jobfinder.features.location.data.mongo.mapper;

import static com.ofdun.jobfinder.support.TestDataMother.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import com.ofdun.jobfinder.features.location.data.mongo.entity.LocationDocument;
import java.util.*;
import org.junit.jupiter.api.*;
import org.mockito.*;

class LocationMongoMapperTest {

    @Test
    @Tag("equivalence")
    void toEntity_whenPresent_mapsAllFields() {
        var model = location();

        var result = LocationMongoMapper.toEntity(model);

        assertEquals(model.getId(), result.getId());
        assertEquals(model.getCity(), result.getCity());
        assertEquals(model.getCountry(), result.getCountry());
    }

    @Test
    @Tag("equivalence")
    void toEntity_whenNull_returnsNull() {

        var result = LocationMongoMapper.toEntity(null);

        assertNull(result);
    }

    @Test
    @Tag("equivalence")
    void toModel_whenPresent_mapsAllFields() {
        var expected = location();
        var entity =
                new LocationDocument(expected.getId(), expected.getCity(), expected.getCountry());

        var result = LocationMongoMapper.toModel(entity);

        assertEquals(expected.getId(), result.getId());
        assertEquals(expected.getCity(), result.getCity());
        assertEquals(expected.getCountry(), result.getCountry());
    }

    @Test
    @Tag("equivalence")
    void toModel_whenNull_returnsNull() {

        var result = LocationMongoMapper.toModel(null);

        assertNull(result);
    }
}
