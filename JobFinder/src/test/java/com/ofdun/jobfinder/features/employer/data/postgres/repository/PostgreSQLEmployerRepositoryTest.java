package com.ofdun.jobfinder.features.employer.data.postgres.repository;

import static com.ofdun.jobfinder.support.TestDataMother.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import com.ofdun.jobfinder.features.employer.data.postgres.entity.EmployerEntity;
import com.ofdun.jobfinder.features.employer.data.postgres.mapper.EmployerMapper;
import java.util.*;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class PostgreSQLEmployerRepositoryTest {
    @Mock private EmployerJpaRepository storage;
    @InjectMocks private PostgreSQLEmployerRepository repository;

    @Test
    @Tag("equivalence")
    void createEmployer_whenValid_savesFields() {
        var model = employer();
        var saved = EmployerMapper.toEntity(model);
        when(storage.save(any(EmployerEntity.class))).thenReturn(saved);

        var result = repository.createEmployer(model);

        assertEquals(1L, result);
        var captor = ArgumentCaptor.forClass(EmployerEntity.class);
        verify(storage).save(captor.capture());
        assertEquals(1L, captor.getValue().getId());
        assertEquals(model.getEmail(), captor.getValue().getEmail());
    }

    @Test
    @Tag("equivalence")
    void createEmployer_whenStorageFails_propagatesFailure() {
        var model = employer();
        var failure = new IllegalStateException("storage unavailable");
        when(storage.save(any(EmployerEntity.class))).thenThrow(failure);

        var error =
                assertThrows(IllegalStateException.class, () -> repository.createEmployer(model));

        assertSame(failure, error);
    }

    @Test
    @Tag("equivalence")
    void updateEmployer_whenValid_savesFields() {
        var model = employer();
        var saved = EmployerMapper.toEntity(model);
        when(storage.save(any(EmployerEntity.class))).thenReturn(saved);

        var result = repository.updateEmployer(model);

        assertEquals(model, result);
        var captor = ArgumentCaptor.forClass(EmployerEntity.class);
        verify(storage).save(captor.capture());
        assertEquals(1L, captor.getValue().getId());
        assertEquals(model.getEmail(), captor.getValue().getEmail());
    }

    @Test
    @Tag("equivalence")
    void updateEmployer_whenStorageFails_propagatesFailure() {
        var model = employer();
        var failure = new IllegalStateException("storage unavailable");
        when(storage.save(any(EmployerEntity.class))).thenThrow(failure);

        var error =
                assertThrows(IllegalStateException.class, () -> repository.updateEmployer(model));

        assertSame(failure, error);
    }

    @Test
    @Tag("equivalence")
    void getEmployerById_whenPresent_mapsFields() {
        var expected = employer();
        when(storage.findById(1L)).thenReturn(Optional.of(EmployerMapper.toEntity(expected)));

        var result = repository.getEmployerById(1L);

        assertEquals(Optional.of(expected), result);
    }

    @Test
    @Tag("equivalence")
    void getEmployerById_whenMissing_returnsEmpty() {
        when(storage.findById(1L)).thenReturn(Optional.empty());

        var result = repository.getEmployerById(1L);

        assertTrue(result.isEmpty());
    }

    @Test
    @Tag("equivalence")
    void getEmployerByEmail_whenPresent_mapsFields() {
        var expected = employer();
        when(storage.findByEmail("user@example.test"))
                .thenReturn(Optional.of(EmployerMapper.toEntity(expected)));

        var result = repository.getEmployerByEmail("user@example.test");

        assertEquals(Optional.of(expected), result);
    }

    @Test
    @Tag("equivalence")
    void getEmployerByEmail_whenMissing_returnsEmpty() {
        when(storage.findByEmail("user@example.test")).thenReturn(Optional.empty());

        var result = repository.getEmployerByEmail("user@example.test");

        assertTrue(result.isEmpty());
    }

    @Test
    @Tag("equivalence")
    void deleteEmployer_whenPresent_deletesRecord() {
        var entity = EmployerMapper.toEntity(employer());
        when(storage.findById(1L)).thenReturn(Optional.of(entity));

        var result = repository.deleteEmployer(1L);

        assertTrue(result);
        verify(storage).delete(entity);
    }

    @Test
    @Tag("equivalence")
    void deleteEmployer_whenMissing_returnsFalse() {
        when(storage.findById(404L)).thenReturn(Optional.empty());

        var result = repository.deleteEmployer(404L);

        assertFalse(result);
        verify(storage, never()).delete(any());
    }
}
