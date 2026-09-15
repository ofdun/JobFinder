package com.ofdun.jobfinder.features.location.domain.service;

import static com.ofdun.jobfinder.support.TestDataMother.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import com.ofdun.jobfinder.features.location.domain.repository.LocationRepository;
import com.ofdun.jobfinder.features.location.exception.LocationNotFoundException;
import java.util.*;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class BasicLocationServiceTest {
    @Mock private LocationRepository repository;
    @InjectMocks private BasicLocationService service;

    @Test
    @Tag("equivalence")
    void getLocationById_whenPresent_returnsModel() {
        var model = location();
        when(repository.getLocationById(1L)).thenReturn(Optional.of(model));

        var result = service.getLocationById(1L);

        assertEquals(model, result);
    }

    @Test
    @Tag("equivalence")
    void getLocationById_whenMissing_throwsNotFound() {
        when(repository.getLocationById(404L)).thenReturn(Optional.empty());

        var error =
                assertThrows(LocationNotFoundException.class, () -> service.getLocationById(404L));

        assertNotNull(error);
    }

    @org.junit.jupiter.params.ParameterizedTest
    @Tag("boundary")
    @org.junit.jupiter.params.provider.CsvSource({"-1,1", "0,1", "1,1", "49,49", "50,50", "51,50"})
    void searchLocations_clampsLimit(int input, int expectedLimit) {
        var expected = List.of(location());
        when(repository.searchLocations("Москва", expectedLimit)).thenReturn(expected);

        var result = service.searchLocations("Москва", input);

        assertEquals(expected, result);
        verify(repository).searchLocations("Москва", expectedLimit);
    }

    @Test
    @Tag("equivalence")
    void searchLocations_whenStorageFails_propagatesFailure() {
        when(repository.searchLocations("Москва", 10))
                .thenThrow(new IllegalStateException("offline"));

        var error =
                assertThrows(
                        IllegalStateException.class, () -> service.searchLocations("Москва", 10));

        assertEquals("offline", error.getMessage());
    }
}
