package com.ofdun.jobfinder.features.applicant.data.mongo.repository;

import static com.ofdun.jobfinder.support.TestDataMother.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import com.ofdun.jobfinder.features.applicant.data.mongo.entity.ApplicantDocument;
import com.ofdun.jobfinder.features.applicant.data.mongo.mapper.ApplicantMongoMapper;
import java.util.*;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class MongoApplicantRepositoryTest {
    @Mock private MongoApplicantCRUDRepository storage;
    @InjectMocks private MongoApplicantRepository repository;

    @Test
    @Tag("equivalence")
    void createApplicant_whenValid_savesFields() {
        var model = applicant();
        var saved = ApplicantMongoMapper.toEntity(model);
        when(storage.save(any(ApplicantDocument.class))).thenReturn(saved);

        var result = repository.createApplicant(model);

        assertEquals(1L, result);
        var captor = ArgumentCaptor.forClass(ApplicantDocument.class);
        verify(storage).save(captor.capture());
        assertEquals(1L, captor.getValue().getId());
        assertEquals(model.getEmail(), captor.getValue().getEmail());
    }

    @Test
    @Tag("equivalence")
    void createApplicant_whenStorageFails_propagatesFailure() {
        var model = applicant();
        var failure = new IllegalStateException("storage unavailable");
        when(storage.save(any(ApplicantDocument.class))).thenThrow(failure);

        var error =
                assertThrows(IllegalStateException.class, () -> repository.createApplicant(model));

        assertSame(failure, error);
    }

    @Test
    @Tag("equivalence")
    void updateApplicant_whenValid_savesFields() {
        var model = applicant();
        var saved = ApplicantMongoMapper.toEntity(model);
        when(storage.save(any(ApplicantDocument.class))).thenReturn(saved);

        var result = repository.updateApplicant(model);

        assertEquals(model, result);
        var captor = ArgumentCaptor.forClass(ApplicantDocument.class);
        verify(storage).save(captor.capture());
        assertEquals(1L, captor.getValue().getId());
        assertEquals(model.getEmail(), captor.getValue().getEmail());
    }

    @Test
    @Tag("equivalence")
    void updateApplicant_whenStorageFails_propagatesFailure() {
        var model = applicant();
        var failure = new IllegalStateException("storage unavailable");
        when(storage.save(any(ApplicantDocument.class))).thenThrow(failure);

        var error =
                assertThrows(IllegalStateException.class, () -> repository.updateApplicant(model));

        assertSame(failure, error);
    }

    @Test
    @Tag("equivalence")
    void getApplicantById_whenPresent_mapsFields() {
        var expected = applicant();
        when(storage.findById(1L)).thenReturn(Optional.of(ApplicantMongoMapper.toEntity(expected)));

        var result = repository.getApplicantById(1L);

        assertEquals(Optional.of(expected), result);
    }

    @Test
    @Tag("equivalence")
    void getApplicantById_whenMissing_returnsEmpty() {
        when(storage.findById(1L)).thenReturn(Optional.empty());

        var result = repository.getApplicantById(1L);

        assertTrue(result.isEmpty());
    }

    @Test
    @Tag("equivalence")
    void getApplicantByEmail_whenPresent_mapsFields() {
        var expected = applicant();
        when(storage.findByEmail("user@example.test"))
                .thenReturn(Optional.of(ApplicantMongoMapper.toEntity(expected)));

        var result = repository.getApplicantByEmail("user@example.test");

        assertEquals(Optional.of(expected), result);
    }

    @Test
    @Tag("equivalence")
    void getApplicantByEmail_whenMissing_returnsEmpty() {
        when(storage.findByEmail("user@example.test")).thenReturn(Optional.empty());

        var result = repository.getApplicantByEmail("user@example.test");

        assertTrue(result.isEmpty());
    }

    @Test
    @Tag("equivalence")
    void deleteApplicant_whenPresent_deletesRecord() {
        var entity = ApplicantMongoMapper.toEntity(applicant());
        when(storage.findById(1L)).thenReturn(Optional.of(entity));

        var result = repository.deleteApplicant(1L);

        assertTrue(result);
        verify(storage).delete(entity);
    }

    @Test
    @Tag("equivalence")
    void deleteApplicant_whenMissing_returnsFalse() {
        when(storage.findById(404L)).thenReturn(Optional.empty());

        var result = repository.deleteApplicant(404L);

        assertFalse(result);
        verify(storage, never()).delete(any());
    }
}
