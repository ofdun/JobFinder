package com.ofdun.jobfinder.features.education.data.mongo.repository;

import static com.ofdun.jobfinder.support.TestDataMother.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import com.ofdun.jobfinder.features.education.data.mongo.mapper.EducationMongoMapper;
import java.util.*;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class MongoEducationRepositoryTest {
    @Mock private MongoEducationCRUDRepository storage;
    @InjectMocks private MongoEducationRepository repository;

    @Test
    @Tag("equivalence")
    void createEducations_whenAvailable_mapsRecords() {
        var model = education();
        when(storage.saveAll(any())).thenReturn(List.of(EducationMongoMapper.toEntity(model)));

        var result = repository.createEducations(List.of(model));

        assertEquals(List.of(model), result);
    }

    @Test
    @Tag("equivalence")
    void createEducations_whenStorageFails_propagatesFailure() {
        var model = education();
        when(storage.saveAll(any())).thenThrow(new IllegalStateException("offline"));

        var error =
                assertThrows(
                        IllegalStateException.class,
                        () -> repository.createEducations(List.of(model)));

        assertEquals("offline", error.getMessage());
    }

    @Test
    @Tag("equivalence")
    void getEducationsByResumeId_whenAvailable_mapsRecords() {
        var model = education();
        when(storage.findAllByResumeId(2L))
                .thenReturn(List.of(EducationMongoMapper.toEntity(model)));

        var result = repository.getEducationsByResumeId(2L);

        assertEquals(List.of(model), result);
    }

    @Test
    @Tag("equivalence")
    void getEducationsByResumeId_whenStorageFails_propagatesFailure() {
        var model = education();
        when(storage.findAllByResumeId(2L)).thenThrow(new IllegalStateException("offline"));

        var error =
                assertThrows(
                        IllegalStateException.class, () -> repository.getEducationsByResumeId(2L));

        assertEquals("offline", error.getMessage());
    }

    @Test
    @Tag("equivalence")
    void deleteAllByResumeId_whenAvailable_removesChildren() {

        repository.deleteAllByResumeId(2L);

        verify(storage).deleteAllByResumeId(2L);
    }

    @Test
    @Tag("equivalence")
    void deleteAllByResumeId_whenStorageFails_propagatesFailure() {
        doThrow(new IllegalStateException("offline")).when(storage).deleteAllByResumeId(2L);

        var error =
                assertThrows(IllegalStateException.class, () -> repository.deleteAllByResumeId(2L));

        assertEquals("offline", error.getMessage());
    }
}
