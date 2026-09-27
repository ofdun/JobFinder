package com.ofdun.jobfinder.features.vacancy.data.postgres.repository;

import static com.ofdun.jobfinder.support.TestDataMother.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import com.ofdun.jobfinder.features.vacancy.data.postgres.mapper.DraftVacancyMapper;
import java.util.*;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class PostgreSQLDraftVacancyRepositoryTest {
    @Mock private DraftVacancyJpaRepository storage;
    @InjectMocks private PostgreSQLDraftVacancyRepository repository;

    @Test
    @Tag("equivalence")
    void save_whenValid_returnsSavedSnapshot() {
        var expected = draft();
        when(storage.save(any())).thenReturn(DraftVacancyMapper.toEntity(expected));

        var result = repository.save(expected);

        assertEquals(expected, result);
    }

    @Test
    @Tag("equivalence")
    void save_whenStorageFails_propagatesFailure() {
        var model = draft();
        when(storage.save(any())).thenThrow(new IllegalStateException("offline"));

        var error = assertThrows(IllegalStateException.class, () -> repository.save(model));

        assertEquals("offline", error.getMessage());
    }

    @Test
    @Tag("equivalence")
    void getByVacancyId_whenPresent_mapsSnapshot() {
        var expected = draft();
        when(storage.findAllByVacancyIdOrderByVersionTimestampDesc(2L))
                .thenReturn(List.of(DraftVacancyMapper.toEntity(expected)));

        var result = repository.getByVacancyId(2L);

        assertEquals(List.of(expected), result);
    }

    @Test
    @Tag("equivalence")
    void getByVacancyId_whenMissing_returnsEmpty() {
        when(storage.findAllByVacancyIdOrderByVersionTimestampDesc(2L)).thenReturn(List.of());

        var result = repository.getByVacancyId(2L);

        assertTrue(result.isEmpty());
    }

    @Test
    @Tag("equivalence")
    void getById_whenPresent_mapsSnapshot() {
        var expected = draft();
        when(storage.findById(1L)).thenReturn(Optional.of(DraftVacancyMapper.toEntity(expected)));

        var result = repository.getById(1L);

        assertEquals(Optional.of(expected), result);
    }

    @Test
    @Tag("equivalence")
    void getById_whenMissing_returnsEmpty() {
        when(storage.findById(1L)).thenReturn(Optional.empty());

        var result = repository.getById(1L);

        assertTrue(result.isEmpty());
    }

    @Test
    @Tag("equivalence")
    void deleteById_whenAvailable_deletesSnapshot() {

        repository.deleteById(1L);

        verify(storage).deleteById(1L);
    }

    @Test
    @Tag("equivalence")
    void deleteById_whenStorageFails_propagatesFailure() {
        doThrow(new IllegalStateException("offline")).when(storage).deleteById(1L);

        var error = assertThrows(IllegalStateException.class, () -> repository.deleteById(1L));

        assertEquals("offline", error.getMessage());
    }
}
