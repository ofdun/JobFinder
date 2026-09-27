package com.ofdun.jobfinder.features.language.domain.service;

import static com.ofdun.jobfinder.support.TestDataMother.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import com.ofdun.jobfinder.features.language.domain.repository.LanguageRepository;
import com.ofdun.jobfinder.features.language.exception.LanguageNotFoundException;
import java.util.*;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class BasicLanguageRepositoryTest {
    @Mock private LanguageRepository repository;
    @InjectMocks private BasicLanguageRepository service;

    @Test
    @Tag("equivalence")
    void getLanguageById_whenPresent_returnsModel() {
        var model = language();
        when(repository.getLanguageById(1L)).thenReturn(Optional.of(model));

        var result = service.getLanguageById(1L);

        assertEquals(model, result);
    }

    @Test
    @Tag("equivalence")
    void getLanguageById_whenMissing_throwsNotFound() {
        when(repository.getLanguageById(404L)).thenReturn(Optional.empty());

        var error =
                assertThrows(LanguageNotFoundException.class, () -> service.getLanguageById(404L));

        assertNotNull(error);
    }

    @Test
    @Tag("equivalence")
    void getAllLanguages_whenPresent_returnsModels() {
        var expected = List.of(language());
        when(repository.getAllLanguages()).thenReturn(expected);

        var result = service.getAllLanguages();

        assertEquals(expected, result);
    }

    @Test
    @Tag("equivalence")
    void getAllLanguages_whenStorageFails_propagatesFailure() {
        var failure = new IllegalStateException("storage unavailable");
        when(repository.getAllLanguages()).thenThrow(failure);

        var error = assertThrows(IllegalStateException.class, service::getAllLanguages);

        assertSame(failure, error);
    }
}
