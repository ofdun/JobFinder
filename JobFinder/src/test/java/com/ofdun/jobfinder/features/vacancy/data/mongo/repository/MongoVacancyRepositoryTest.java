package com.ofdun.jobfinder.features.vacancy.data.mongo.repository;

import static com.ofdun.jobfinder.support.TestDataMother.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import com.ofdun.jobfinder.features.vacancy.data.mongo.entity.VacancyDocument;
import com.ofdun.jobfinder.features.vacancy.data.mongo.mapper.VacancyMongoMapper;
import java.util.*;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class MongoVacancyRepositoryTest {
    @Mock private MongoVacancyCRUDRepository storage;
    @InjectMocks private MongoVacancyRepository repository;

    @Test
    @Tag("equivalence")
    void createVacancy_whenValid_savesFields() {
        var model = vacancy();
        var saved = VacancyMongoMapper.toEntity(model);
        when(storage.save(any(VacancyDocument.class))).thenReturn(saved);

        var result = repository.createVacancy(model);

        assertEquals(1L, result);
        var captor = ArgumentCaptor.forClass(VacancyDocument.class);
        verify(storage).save(captor.capture());
        assertEquals(1L, captor.getValue().getId());
        assertEquals(model.getSalary(), captor.getValue().getSalary());
    }

    @Test
    @Tag("equivalence")
    void createVacancy_whenStorageFails_propagatesFailure() {
        var model = vacancy();
        var failure = new IllegalStateException("storage unavailable");
        when(storage.save(any(VacancyDocument.class))).thenThrow(failure);

        var error =
                assertThrows(IllegalStateException.class, () -> repository.createVacancy(model));

        assertSame(failure, error);
    }

    @Test
    @Tag("equivalence")
    void updateVacancy_whenValid_savesFields() {
        var model = vacancy();
        var saved = VacancyMongoMapper.toEntity(model);
        when(storage.save(any(VacancyDocument.class))).thenReturn(saved);

        var result = repository.updateVacancy(model);

        assertEquals(model, result);
        var captor = ArgumentCaptor.forClass(VacancyDocument.class);
        verify(storage).save(captor.capture());
        assertEquals(1L, captor.getValue().getId());
        assertEquals(model.getSalary(), captor.getValue().getSalary());
    }

    @Test
    @Tag("equivalence")
    void updateVacancy_whenStorageFails_propagatesFailure() {
        var model = vacancy();
        var failure = new IllegalStateException("storage unavailable");
        when(storage.save(any(VacancyDocument.class))).thenThrow(failure);

        var error =
                assertThrows(IllegalStateException.class, () -> repository.updateVacancy(model));

        assertSame(failure, error);
    }

    @Test
    @Tag("equivalence")
    void getVacancyById_whenPresent_mapsFields() {
        var expected = vacancy();
        when(storage.findById(1L)).thenReturn(Optional.of(VacancyMongoMapper.toEntity(expected)));

        var result = repository.getVacancyById(1L);

        assertEquals(Optional.of(expected), result);
    }

    @Test
    @Tag("equivalence")
    void getVacancyById_whenMissing_returnsEmpty() {
        when(storage.findById(1L)).thenReturn(Optional.empty());

        var result = repository.getVacancyById(1L);

        assertTrue(result.isEmpty());
    }

    @Test
    @Tag("equivalence")
    void deleteVacancy_whenPresent_deletesRecord() {
        var entity = VacancyMongoMapper.toEntity(vacancy());
        when(storage.findById(1L)).thenReturn(Optional.of(entity));

        var result = repository.deleteVacancy(1L);

        assertTrue(result);
        verify(storage).delete(entity);
    }

    @Test
    @Tag("equivalence")
    void deleteVacancy_whenMissing_returnsFalse() {
        when(storage.findById(404L)).thenReturn(Optional.empty());

        var result = repository.deleteVacancy(404L);

        assertFalse(result);
        verify(storage, never()).delete(any());
    }

    @Test
    @Tag("equivalence")
    void searchVacancies_whenMatching_returnsPage() {
        var model = vacancy();
        when(storage.findAll()).thenReturn(List.of(VacancyMongoMapper.toEntity(model)));

        var result = repository.searchVacancies(null, null);

        assertEquals(List.of(model), result.getItems());
        assertEquals(1L, result.getTotalElements());
    }

    @Test
    @Tag("equivalence")
    void searchVacancies_whenNotMatching_returnsEmptyPage() {
        when(storage.findAll()).thenReturn(List.of(VacancyMongoMapper.toEntity(vacancy())));
        var filter = new com.ofdun.jobfinder.features.vacancy.domain.model.VacancySearchFilter();
        filter.setEmployerId(999L);

        var result = repository.searchVacancies(filter, null);

        assertTrue(result.getItems().isEmpty());
        assertEquals(0L, result.getTotalElements());
    }
}
