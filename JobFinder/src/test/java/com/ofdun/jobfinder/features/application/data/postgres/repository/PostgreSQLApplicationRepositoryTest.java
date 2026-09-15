package com.ofdun.jobfinder.features.application.data.postgres.repository;

import static com.ofdun.jobfinder.support.TestDataMother.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import com.ofdun.jobfinder.features.application.data.postgres.entity.ApplicationEntity;
import com.ofdun.jobfinder.features.application.data.postgres.mapper.ApplicationPostgreSQLMapper;
import java.util.*;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class PostgreSQLApplicationRepositoryTest {
    @Mock private ApplicationJpaRepository storage;
    @InjectMocks private PostgreSQLApplicationRepository repository;

    @Test
    @Tag("equivalence")
    void createApplication_whenValid_savesFields() {
        var model = application();
        var saved = ApplicationPostgreSQLMapper.toEntity(model);
        when(storage.save(any(ApplicationEntity.class))).thenReturn(saved);

        var result = repository.createApplication(model);

        assertEquals(1L, result);
        var captor = ArgumentCaptor.forClass(ApplicationEntity.class);
        verify(storage).save(captor.capture());
        assertEquals(1L, captor.getValue().getId());
        assertEquals(model.getResumeId(), captor.getValue().getResumeId());
    }

    @Test
    @Tag("equivalence")
    void createApplication_whenStorageFails_propagatesFailure() {
        var model = application();
        var failure = new IllegalStateException("storage unavailable");
        when(storage.save(any(ApplicationEntity.class))).thenThrow(failure);

        var error =
                assertThrows(
                        IllegalStateException.class, () -> repository.createApplication(model));

        assertSame(failure, error);
    }

    @Test
    @Tag("equivalence")
    void updateApplication_whenValid_savesFields() {
        var model = application();
        var saved = ApplicationPostgreSQLMapper.toEntity(model);
        when(storage.save(any(ApplicationEntity.class))).thenReturn(saved);

        var result = repository.updateApplication(model);

        assertEquals(model, result);
        var captor = ArgumentCaptor.forClass(ApplicationEntity.class);
        verify(storage).save(captor.capture());
        assertEquals(1L, captor.getValue().getId());
        assertEquals(model.getResumeId(), captor.getValue().getResumeId());
    }

    @Test
    @Tag("equivalence")
    void updateApplication_whenStorageFails_propagatesFailure() {
        var model = application();
        var failure = new IllegalStateException("storage unavailable");
        when(storage.save(any(ApplicationEntity.class))).thenThrow(failure);

        var error =
                assertThrows(
                        IllegalStateException.class, () -> repository.updateApplication(model));

        assertSame(failure, error);
    }

    @Test
    @Tag("equivalence")
    void getApplicationById_whenPresent_mapsFields() {
        var expected = application();
        when(storage.findById(1L))
                .thenReturn(Optional.of(ApplicationPostgreSQLMapper.toEntity(expected)));

        var result = repository.getApplicationById(1L);

        assertEquals(Optional.of(expected), result);
    }

    @Test
    @Tag("equivalence")
    void getApplicationById_whenMissing_returnsEmpty() {
        when(storage.findById(1L)).thenReturn(Optional.empty());

        var result = repository.getApplicationById(1L);

        assertTrue(result.isEmpty());
    }

    @Test
    @Tag("equivalence")
    void deleteApplication_whenPresent_deletesRecord() {
        var entity = ApplicationPostgreSQLMapper.toEntity(application());
        when(storage.findById(1L)).thenReturn(Optional.of(entity));

        var result = repository.deleteApplication(1L);

        assertTrue(result);
        verify(storage).delete(entity);
    }

    @Test
    @Tag("equivalence")
    void deleteApplication_whenMissing_returnsFalse() {
        when(storage.findById(404L)).thenReturn(Optional.empty());

        var result = repository.deleteApplication(404L);

        assertFalse(result);
        verify(storage, never()).delete(any());
    }

    @Test
    @Tag("equivalence")
    void getApplicationsByVacancyId_whenPresent_mapsRecords() {
        var expected = application();
        when(storage.findByVacancyIdOrderByDateDesc(2L))
                .thenReturn(List.of(ApplicationPostgreSQLMapper.toEntity(expected)));

        var result = repository.getApplicationsByVacancyId(2L);

        assertEquals(List.of(expected), result);
    }

    @Test
    @Tag("equivalence")
    void getApplicationsByVacancyId_whenStorageFails_propagatesFailure() {
        when(storage.findByVacancyIdOrderByDateDesc(2L))
                .thenThrow(new IllegalStateException("offline"));

        var error =
                assertThrows(
                        IllegalStateException.class,
                        () -> repository.getApplicationsByVacancyId(2L));

        assertEquals("offline", error.getMessage());
    }
}
