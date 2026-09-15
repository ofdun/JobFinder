package com.ofdun.jobfinder.features.location.data.postgres.mapper;

import static com.ofdun.jobfinder.support.TestDataMother.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import com.ofdun.jobfinder.features.location.data.postgres.entity.LocationEntity;
import java.util.*;
import org.junit.jupiter.api.*;
import org.mockito.*;

class LocationMapperTest {

    @Test
    @Tag("equivalence")
    void toEntity_whenPresent_mapsAllFields() {
        var model = location();

        var result = LocationMapper.toEntity(model);

        assertEquals(model.getId(), result.getId());
        assertEquals(model.getCity(), result.getCity());
        assertEquals(model.getCountry(), result.getCountry());
    }

    @Test
    @Tag("equivalence")
    void toEntity_whenNull_returnsNull() {

        var result = LocationMapper.toEntity(null);

        assertNull(result);
    }

    @Test
    @Tag("equivalence")
    void toModel_whenPresent_mapsAllFields() {
        var expected = location();
        var entity =
                new LocationEntity(expected.getId(), expected.getCity(), expected.getCountry());

        var result = LocationMapper.toModel(entity);

        assertEquals(expected.getId(), result.getId());
        assertEquals(expected.getCity(), result.getCity());
        assertEquals(expected.getCountry(), result.getCountry());
    }

    @Test
    @Tag("equivalence")
    void toModel_whenNull_returnsNull() {

        var result = LocationMapper.toModel(null);

        assertNull(result);
    }
}
