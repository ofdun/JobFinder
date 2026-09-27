package com.ofdun.jobfinder.features.location.data.mongo.repository;

import static com.ofdun.jobfinder.support.TestDataMother.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import com.ofdun.jobfinder.features.location.data.mongo.mapper.LocationMongoMapper;
import java.util.*;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class MongoLocationRepositoryTest {
    @Mock private MongoLocationCRUDRepository storage;
    @InjectMocks private MongoLocationRepository repository;

    @Test
    @Tag("equivalence")
    void getLocationById_whenPresent_mapsFields() {
        var expected = location();
        when(storage.findById(1L)).thenReturn(Optional.of(LocationMongoMapper.toEntity(expected)));

        var result = repository.getLocationById(1L);

        assertEquals(Optional.of(expected), result);
    }

    @Test
    @Tag("equivalence")
    void getLocationById_whenMissing_returnsEmpty() {
        when(storage.findById(404L)).thenReturn(Optional.empty());

        var result = repository.getLocationById(404L);

        assertTrue(result.isEmpty());
    }

    @Test
    @Tag("equivalence")
    void searchLocations_trimsQueryAndUsesLimit() {
        var expected = location();
        when(storage.findByCityContainingIgnoreCaseOrCountryContainingIgnoreCaseOrderByCityAsc(
                        "Москва", "Москва", org.springframework.data.domain.PageRequest.of(0, 5)))
                .thenReturn(List.of(LocationMongoMapper.toEntity(expected)));

        var result = repository.searchLocations("  Москва  ", 5);

        assertEquals(List.of(expected), result);
    }

    @org.junit.jupiter.params.ParameterizedTest
    @Tag("equivalence")
    @org.junit.jupiter.params.provider.NullAndEmptySource
    @org.junit.jupiter.params.provider.ValueSource(strings = {" ", "\t"})
    void searchLocations_blankQuerySkipsStorage(String query) {
        var result = repository.searchLocations(query, 5);

        assertTrue(result.isEmpty());
        verifyNoInteractions(storage);
    }
}
