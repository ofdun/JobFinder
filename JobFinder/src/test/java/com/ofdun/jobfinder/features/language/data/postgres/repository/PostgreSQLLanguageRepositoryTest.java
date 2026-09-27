package com.ofdun.jobfinder.features.language.data.postgres.repository;

import static com.ofdun.jobfinder.support.TestDataMother.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import com.ofdun.jobfinder.features.language.data.postgres.mapper.LanguageMapper;
import java.util.*;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class PostgreSQLLanguageRepositoryTest {
    @Mock private LanguageJpaRepository storage;
    @InjectMocks private PostgreSQLLanguageRepository repository;

    @Test
    @Tag("equivalence")
    void getLanguageById_whenPresent_mapsFields() {
        var expected = language();
        when(storage.findById(1L)).thenReturn(Optional.of(LanguageMapper.toEntity(expected)));

        var result = repository.getLanguageById(1L);

        assertEquals(Optional.of(expected), result);
    }

    @Test
    @Tag("equivalence")
    void getLanguageById_whenMissing_returnsEmpty() {
        when(storage.findById(404L)).thenReturn(Optional.empty());

        var result = repository.getLanguageById(404L);

        assertTrue(result.isEmpty());
    }

    @Test
    @Tag("equivalence")
    void getAllLanguages_whenPresent_preservesFields() {
        var expected = language();
        when(storage.findAllByOrderByNameAsc())
                .thenReturn(List.of(LanguageMapper.toEntity(expected)));

        var result = repository.getAllLanguages();

        assertEquals(List.of(expected), result);
    }

    @Test
    @Tag("equivalence")
    void getAllLanguages_whenStorageFails_propagatesFailure() {
        var failure = new IllegalStateException("storage unavailable");
        when(storage.findAllByOrderByNameAsc()).thenThrow(failure);

        var error = assertThrows(IllegalStateException.class, repository::getAllLanguages);

        assertSame(failure, error);
    }
}
