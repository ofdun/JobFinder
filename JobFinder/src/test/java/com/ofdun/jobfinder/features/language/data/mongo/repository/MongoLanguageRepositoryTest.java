package com.ofdun.jobfinder.features.language.data.mongo.repository;

import static com.ofdun.jobfinder.support.TestDataMother.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import com.ofdun.jobfinder.features.language.data.mongo.mapper.LanguageMongoMapper;
import java.util.*;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class MongoLanguageRepositoryTest {
    @Mock private MongoLanguageCRUDRepository storage;
    @InjectMocks private MongoLanguageRepository repository;

    @Test
    @Tag("equivalence")
    void getLanguageById_whenPresent_mapsFields() {
        var expected = language();
        when(storage.findById(1L)).thenReturn(Optional.of(LanguageMongoMapper.toEntity(expected)));

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
                .thenReturn(List.of(LanguageMongoMapper.toEntity(expected)));

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
