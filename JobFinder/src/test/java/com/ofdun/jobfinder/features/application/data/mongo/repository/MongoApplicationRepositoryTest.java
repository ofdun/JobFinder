package com.ofdun.jobfinder.features.application.data.mongo.repository;

import static com.ofdun.jobfinder.support.TestDataMother.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import com.ofdun.jobfinder.features.application.data.mongo.entity.ApplicationDocument;
import com.ofdun.jobfinder.features.application.data.mongo.mapper.ApplicationMongoMapper;
import java.util.*;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class MongoApplicationRepositoryTest {
    @Mock private MongoApplicationCRUDRepository storage;
    @InjectMocks private MongoApplicationRepository repository;

    @Test
    @Tag("equivalence")
    void createApplication_whenValid_savesFields() {
        var model = application();
        var saved = ApplicationMongoMapper.toEntity(model);
        when(storage.save(any(ApplicationDocument.class))).thenReturn(saved);

        var result = repository.createApplication(model);

        assertEquals(1L, result);
        var captor = ArgumentCaptor.forClass(ApplicationDocument.class);
        verify(storage).save(captor.capture());
        assertEquals(1L, captor.getValue().getId());
        assertEquals(model.getResumeId(), captor.getValue().getResumeId());
    }

    @Test
    @Tag("equivalence")
    void createApplication_whenStorageFails_propagatesFailure() {
        var model = application();
        var failure = new IllegalStateException("storage unavailable");
        when(storage.save(any(ApplicationDocument.class))).thenThrow(failure);

        var error =
                assertThrows(
                        IllegalStateException.class, () -> repository.createApplication(model));

        assertSame(failure, error);
    }

    @Test
    @Tag("equivalence")
    void updateApplication_whenValid_savesFields() {
        var model = application();
        var saved = ApplicationMongoMapper.toEntity(model);
        when(storage.save(any(ApplicationDocument.class))).thenReturn(saved);

        var result = repository.updateApplication(model);

        assertEquals(model, result);
        var captor = ArgumentCaptor.forClass(ApplicationDocument.class);
        verify(storage).save(captor.capture());
        assertEquals(1L, captor.getValue().getId());
        assertEquals(model.getResumeId(), captor.getValue().getResumeId());
    }

    @Test
    @Tag("equivalence")
    void updateApplication_whenStorageFails_propagatesFailure() {
        var model = application();
        var failure = new IllegalStateException("storage unavailable");
        when(storage.save(any(ApplicationDocument.class))).thenThrow(failure);

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
                .thenReturn(Optional.of(ApplicationMongoMapper.toEntity(expected)));

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
        var entity = ApplicationMongoMapper.toEntity(application());
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
        when(storage.findByVacancyIdOrderByApplicationDateDesc(2L))
                .thenReturn(List.of(ApplicationMongoMapper.toEntity(expected)));

        var result = repository.getApplicationsByVacancyId(2L);

        assertEquals(List.of(expected), result);
    }

    @Test
    @Tag("equivalence")
    void getApplicationsByVacancyId_whenStorageFails_propagatesFailure() {
        when(storage.findByVacancyIdOrderByApplicationDateDesc(2L))
                .thenThrow(new IllegalStateException("offline"));

        var error =
                assertThrows(
                        IllegalStateException.class,
                        () -> repository.getApplicationsByVacancyId(2L));

        assertEquals("offline", error.getMessage());
    }
}
