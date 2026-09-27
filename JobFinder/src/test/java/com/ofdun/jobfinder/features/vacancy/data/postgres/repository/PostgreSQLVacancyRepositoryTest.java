package com.ofdun.jobfinder.features.vacancy.data.postgres.repository;

import static com.ofdun.jobfinder.support.TestDataMother.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import com.ofdun.jobfinder.features.vacancy.data.postgres.entity.VacancyEntity;
import com.ofdun.jobfinder.features.vacancy.data.postgres.mapper.VacancyMapper;
import java.util.*;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class PostgreSQLVacancyRepositoryTest {
    @BeforeEach
    void setUpEntityManager() {
        org.springframework.test.util.ReflectionTestUtils.setField(
                repository, "entityManager", entityManager);
    }

    @Mock private jakarta.persistence.EntityManager entityManager;
    @Mock private jakarta.persistence.criteria.CriteriaBuilder criteria;

    @Mock
    private jakarta.persistence.criteria.CriteriaQuery<
                    com.ofdun.jobfinder.features.vacancy.data.postgres.entity.VacancyEntity>
            query;

    @Mock
    private jakarta.persistence.criteria.Root<
                    com.ofdun.jobfinder.features.vacancy.data.postgres.entity.VacancyEntity>
            root;

    @Mock
    private jakarta.persistence.TypedQuery<
                    com.ofdun.jobfinder.features.vacancy.data.postgres.entity.VacancyEntity>
            typed;

    @Test
    @Tag("equivalence")
    void searchVacancies_whenAvailable_appliesOffsetAndLimit() {
        var expected = vacancy();
        when(storage.count(any(org.springframework.data.jpa.domain.Specification.class)))
                .thenReturn(3L);
        when(entityManager.getCriteriaBuilder()).thenReturn(criteria);
        when(criteria.createQuery(
                        com.ofdun.jobfinder.features.vacancy.data.postgres.entity.VacancyEntity
                                .class))
                .thenReturn(query);
        when(query.from(
                        com.ofdun.jobfinder.features.vacancy.data.postgres.entity.VacancyEntity
                                .class))
                .thenReturn(root);
        when(entityManager.createQuery(query)).thenReturn(typed);
        when(typed.getResultList()).thenReturn(List.of(VacancyMapper.toEntity(expected)));

        var result =
                repository.searchVacancies(
                        null,
                        com.ofdun.jobfinder.common.domain.model.OffsetPagination.builder()
                                .limit(2)
                                .offset(2)
                                .build());

        assertEquals(3L, result.getTotalElements());
        assertEquals(2, result.getTotalPages());
        assertEquals(1, result.getPage());
        assertEquals(2, result.getSize());
        assertEquals(expected.getDescription(), result.getItems().getFirst().getDescription());
        verify(typed).setFirstResult(2);
        verify(typed).setMaxResults(2);
    }

    @Test
    @Tag("equivalence")
    void searchVacancies_whenStorageFails_propagatesFailure() {
        when(storage.count(any(org.springframework.data.jpa.domain.Specification.class)))
                .thenThrow(new IllegalStateException("offline"));

        var error =
                assertThrows(
                        IllegalStateException.class,
                        () ->
                                repository.searchVacancies(
                                        null,
                                        com.ofdun.jobfinder.common.domain.model.OffsetPagination
                                                .builder()
                                                .limit(2)
                                                .offset(2)
                                                .build()));

        assertEquals("offline", error.getMessage());
        verifyNoInteractions(entityManager);
    }

    @Mock private VacancyJpaRepository storage;
    @InjectMocks private PostgreSQLVacancyRepository repository;

    @Test
    @Tag("equivalence")
    void createVacancy_whenValid_savesFields() {
        var model = vacancy();
        var saved = VacancyMapper.toEntity(model);
        when(storage.save(any(VacancyEntity.class))).thenReturn(saved);

        var result = repository.createVacancy(model);

        assertEquals(1L, result);
        var captor = ArgumentCaptor.forClass(VacancyEntity.class);
        verify(storage).save(captor.capture());
        assertEquals(1L, captor.getValue().getId());
        assertEquals(model.getSalary(), captor.getValue().getSalary());
    }

    @Test
    @Tag("equivalence")
    void createVacancy_whenStorageFails_propagatesFailure() {
        var model = vacancy();
        var failure = new IllegalStateException("storage unavailable");
        when(storage.save(any(VacancyEntity.class))).thenThrow(failure);

        var error =
                assertThrows(IllegalStateException.class, () -> repository.createVacancy(model));

        assertSame(failure, error);
    }

    @Test
    @Tag("equivalence")
    void updateVacancy_whenValid_savesFields() {
        var model = vacancy();
        var saved = VacancyMapper.toEntity(model);
        when(storage.save(any(VacancyEntity.class))).thenReturn(saved);

        var result = repository.updateVacancy(model);

        assertEquals(model, result);
        var captor = ArgumentCaptor.forClass(VacancyEntity.class);
        verify(storage).save(captor.capture());
        assertEquals(1L, captor.getValue().getId());
        assertEquals(model.getSalary(), captor.getValue().getSalary());
    }

    @Test
    @Tag("equivalence")
    void updateVacancy_whenStorageFails_propagatesFailure() {
        var model = vacancy();
        var failure = new IllegalStateException("storage unavailable");
        when(storage.save(any(VacancyEntity.class))).thenThrow(failure);

        var error =
                assertThrows(IllegalStateException.class, () -> repository.updateVacancy(model));

        assertSame(failure, error);
    }

    @Test
    @Tag("equivalence")
    void getVacancyById_whenPresent_mapsFields() {
        var expected = vacancy();
        when(storage.findById(1L)).thenReturn(Optional.of(VacancyMapper.toEntity(expected)));

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
        var entity = VacancyMapper.toEntity(vacancy());
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
        verify(storage, never()).delete(any(VacancyEntity.class));
    }
}
