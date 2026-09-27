package com.ofdun.jobfinder.features.experience.data.mongo.repository;

import static com.ofdun.jobfinder.support.TestDataMother.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import com.ofdun.jobfinder.features.experience.data.mongo.mapper.JobExperienceMongoMapper;
import java.util.*;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class MongoJobExperienceRepositoryTest {
    @Mock private MongoJobExperienceCRUDRepository storage;
    @InjectMocks private MongoJobExperienceRepository repository;

    @Test
    @Tag("equivalence")
    void createJobExperiences_whenAvailable_mapsRecords() {
        var model = experience();
        when(storage.saveAll(any())).thenReturn(List.of(JobExperienceMongoMapper.toEntity(model)));

        var result = repository.createJobExperiences(List.of(model));

        assertEquals(List.of(model), result);
    }

    @Test
    @Tag("equivalence")
    void createJobExperiences_whenStorageFails_propagatesFailure() {
        var model = experience();
        when(storage.saveAll(any())).thenThrow(new IllegalStateException("offline"));

        var error =
                assertThrows(
                        IllegalStateException.class,
                        () -> repository.createJobExperiences(List.of(model)));

        assertEquals("offline", error.getMessage());
    }

    @Test
    @Tag("equivalence")
    void getJobExperiencesByResumeId_whenAvailable_mapsRecords() {
        var model = experience();
        when(storage.findAllByResumeId(2L))
                .thenReturn(List.of(JobExperienceMongoMapper.toEntity(model)));

        var result = repository.getJobExperiencesByResumeId(2L);

        assertEquals(List.of(model), result);
    }

    @Test
    @Tag("equivalence")
    void getJobExperiencesByResumeId_whenStorageFails_propagatesFailure() {
        var model = experience();
        when(storage.findAllByResumeId(2L)).thenThrow(new IllegalStateException("offline"));

        var error =
                assertThrows(
                        IllegalStateException.class,
                        () -> repository.getJobExperiencesByResumeId(2L));

        assertEquals("offline", error.getMessage());
    }

    @Test
    @Tag("equivalence")
    void createJobExperience_whenAvailable_mapsRecords() {
        var model = experience();
        when(storage.save(any())).thenReturn(JobExperienceMongoMapper.toEntity(model));

        var result = repository.createJobExperience(model);

        assertEquals(1L, result);
    }

    @Test
    @Tag("equivalence")
    void createJobExperience_whenStorageFails_propagatesFailure() {
        var model = experience();
        when(storage.save(any())).thenThrow(new IllegalStateException("offline"));

        var error =
                assertThrows(
                        IllegalStateException.class, () -> repository.createJobExperience(model));

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
