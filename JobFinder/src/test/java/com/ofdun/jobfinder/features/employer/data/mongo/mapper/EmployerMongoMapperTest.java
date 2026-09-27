package com.ofdun.jobfinder.features.employer.data.mongo.mapper;

import static com.ofdun.jobfinder.support.TestDataMother.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import com.ofdun.jobfinder.features.employer.data.mongo.entity.EmployerDocument;
import java.util.*;
import org.junit.jupiter.api.*;
import org.mockito.*;

class EmployerMongoMapperTest {

    @Test
    @Tag("equivalence")
    void toEntity_whenPresent_mapsAllFields() {
        var model = employer();

        var result = EmployerMongoMapper.toEntity(model);

        assertEquals(model.getId(), result.getId());
        assertEquals(model.getName(), result.getName());
        assertEquals(model.getPasswordHash(), result.getPasswordHash());
        assertEquals(model.getDescription(), result.getDescription());
        assertEquals(model.getAddress(), result.getAddress());
        assertEquals(model.getWebsiteLink(), result.getWebsiteLink());
        assertEquals(model.getEmail(), result.getEmail());
        assertEquals(model.getLocationId(), result.getLocationId());
    }

    @Test
    @Tag("equivalence")
    void toEntity_whenNull_returnsNull() {

        var result = EmployerMongoMapper.toEntity(null);

        assertNull(result);
    }

    @Test
    @Tag("equivalence")
    void toModel_whenPresent_mapsAllFields() {
        var expected = employer();
        var entity =
                new EmployerDocument(
                        expected.getId(),
                        expected.getName(),
                        expected.getPasswordHash(),
                        expected.getDescription(),
                        expected.getAddress(),
                        expected.getWebsiteLink(),
                        expected.getEmail(),
                        expected.getLocationId());

        var result = EmployerMongoMapper.toModel(entity);

        assertEquals(expected.getId(), result.getId());
        assertEquals(expected.getName(), result.getName());
        assertEquals(expected.getPasswordHash(), result.getPasswordHash());
        assertEquals(expected.getDescription(), result.getDescription());
        assertEquals(expected.getAddress(), result.getAddress());
        assertEquals(expected.getWebsiteLink(), result.getWebsiteLink());
        assertEquals(expected.getEmail(), result.getEmail());
        assertEquals(expected.getLocationId(), result.getLocationId());
    }

    @Test
    @Tag("equivalence")
    void toModel_whenNull_returnsNull() {

        var result = EmployerMongoMapper.toModel(null);

        assertNull(result);
    }
}
