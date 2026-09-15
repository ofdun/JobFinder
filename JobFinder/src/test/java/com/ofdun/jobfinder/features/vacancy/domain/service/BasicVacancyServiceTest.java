package com.ofdun.jobfinder.features.vacancy.domain.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import com.ofdun.jobfinder.features.vacancy.domain.model.VacancyModel;
import com.ofdun.jobfinder.features.vacancy.domain.repository.VacancyRepository;
import com.ofdun.jobfinder.features.vacancy.domain.validator.VacancyValidator;
import com.ofdun.jobfinder.features.vacancy.exception.VacancyNotFoundException;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
@org.junit.jupiter.api.Tag("equivalence")
class BasicVacancyServiceTest {
    @Test
    @org.junit.jupiter.api.Tag("equivalence")
    void searchVacancies_whenValid_returnsPage() {
        var expected =
                new com.ofdun.jobfinder.common.domain.model.PageResult<VacancyModel>(
                        java.util.List.of(com.ofdun.jobfinder.support.TestDataMother.vacancy()),
                        0,
                        20,
                        1L,
                        1);
        when(vacancyRepository.searchVacancies(isNull(), any())).thenReturn(expected);

        var result = vacancyService.searchVacancies(null, null);

        assertSame(expected, result);
    }

    @Test
    @org.junit.jupiter.api.Tag("equivalence")
    void searchVacancies_whenSalaryRangeReversed_rejectsFilter() {
        var filter = new com.ofdun.jobfinder.features.vacancy.domain.model.VacancySearchFilter();
        filter.setSalaryMin(new java.math.BigDecimal("101"));
        filter.setSalaryMax(new java.math.BigDecimal("100"));

        var error =
                assertThrows(
                        IllegalArgumentException.class,
                        () -> vacancyService.searchVacancies(filter, null));

        assertEquals("salaryMin must be <= salaryMax", error.getMessage());
        verifyNoInteractions(vacancyRepository);
    }

    @org.junit.jupiter.params.ParameterizedTest
    @org.junit.jupiter.api.Tag("boundary")
    @org.junit.jupiter.params.provider.CsvSource({
        "0,-1,1,0",
        "1,0,1,0",
        "100,1,100,1",
        "101,10,100,10"
    })
    void searchVacancies_clampsPagination(
            int limit, int offset, int expectedLimit, int expectedOffset) {
        var pagination =
                com.ofdun.jobfinder.common.domain.model.OffsetPagination.builder()
                        .limit(limit)
                        .offset(offset)
                        .sortBy("salary")
                        .sortDesc(true)
                        .build();

        vacancyService.searchVacancies(null, pagination);

        var captor =
                org.mockito.ArgumentCaptor.forClass(
                        com.ofdun.jobfinder.common.domain.model.OffsetPagination.class);
        verify(vacancyRepository).searchVacancies(isNull(), captor.capture());
        assertEquals(expectedLimit, captor.getValue().getLimit());
        assertEquals(expectedOffset, captor.getValue().getOffset());
        assertEquals("salary", captor.getValue().getSortBy());
        assertTrue(captor.getValue().isSortDesc());
    }

    @Test
    @org.junit.jupiter.api.Tag("equivalence")
    void createVacancy_whenValidationFails_skipsStorage() {
        var input = com.ofdun.jobfinder.support.TestDataMother.vacancy();
        doThrow(new IllegalArgumentException("invalid"))
                .when(vacancyValidator)
                .validateVacancyForCreate(input);

        var error =
                assertThrows(
                        IllegalArgumentException.class, () -> vacancyService.createVacancy(input));

        assertEquals("invalid", error.getMessage());
        verifyNoInteractions(vacancyRepository);
    }

    @Test
    @org.junit.jupiter.api.Tag("equivalence")
    void deleteVacancy_whenValidationFails_skipsStorage() {
        Long input = 0L;
        doThrow(new IllegalArgumentException("invalid"))
                .when(vacancyValidator)
                .validateVacancyForDelete(input);

        var error =
                assertThrows(
                        IllegalArgumentException.class, () -> vacancyService.deleteVacancy(input));

        assertEquals("invalid", error.getMessage());
        verifyNoInteractions(vacancyRepository);
    }

    @Mock private VacancyRepository vacancyRepository;

    @Mock private VacancyValidator vacancyValidator;

    @InjectMocks private BasicVacancyService vacancyService;

    @Test
    void createVacancy_whenValidVacancy_thenIdReturned() {
        VacancyModel vacancyModel = mock(VacancyModel.class);
        Long expectedId = 1L;
        doNothing().when(vacancyValidator).validateVacancyForCreate(vacancyModel);
        when(vacancyRepository.createVacancy(vacancyModel)).thenReturn(expectedId);

        Long actualId = vacancyService.createVacancy(vacancyModel);

        assertEquals(expectedId, actualId);
        verify(vacancyValidator).validateVacancyForCreate(vacancyModel);
        verify(vacancyRepository).createVacancy(vacancyModel);
    }

    @Test
    void createVacancy_whenRepositoryReturnsNull_thenNullReturned() {
        VacancyModel vacancyModel = mock(VacancyModel.class);
        doNothing().when(vacancyValidator).validateVacancyForCreate(vacancyModel);
        when(vacancyRepository.createVacancy(vacancyModel)).thenReturn(null);

        Long actualId = vacancyService.createVacancy(vacancyModel);

        assertNull(actualId);
        verify(vacancyValidator).validateVacancyForCreate(vacancyModel);
        verify(vacancyRepository).createVacancy(vacancyModel);
    }

    @Test
    void getVacancyById_whenExists_thenVacancyReturned() {
        Long id = 1L;
        VacancyModel expectedVacancy = mock(VacancyModel.class);
        when(vacancyRepository.getVacancyById(id)).thenReturn(Optional.of(expectedVacancy));

        VacancyModel actualVacancy = vacancyService.getVacancyById(id);

        assertSame(expectedVacancy, actualVacancy);
        verify(vacancyRepository).getVacancyById(id);
        verifyNoMoreInteractions(vacancyRepository);
    }

    @Test
    void getVacancyById_whenMissing_thenThrowsNotFound() {
        Long id = 404L;
        when(vacancyRepository.getVacancyById(id)).thenReturn(Optional.empty());

        assertThrows(VacancyNotFoundException.class, () -> vacancyService.getVacancyById(id));

        verify(vacancyRepository).getVacancyById(id);
        verifyNoMoreInteractions(vacancyRepository);
    }

    @Test
    void updateVacancy_whenValidVacancy_thenRepositoryCalledAndModelReturned() {
        VacancyModel vacancyModel = mock(VacancyModel.class);
        doNothing().when(vacancyValidator).validateVacancyForUpdate(vacancyModel);
        when(vacancyRepository.updateVacancy(vacancyModel)).thenReturn(vacancyModel);

        VacancyModel actual = vacancyService.updateVacancy(vacancyModel);

        assertSame(vacancyModel, actual);
        verify(vacancyValidator).validateVacancyForUpdate(vacancyModel);
        verify(vacancyRepository).updateVacancy(vacancyModel);
    }

    @Test
    void updateVacancy_whenInvalidVacancy_thenThrowsIllegalArgumentException() {
        VacancyModel vacancyModel = mock(VacancyModel.class);
        doThrow(new IllegalArgumentException())
                .when(vacancyValidator)
                .validateVacancyForUpdate(vacancyModel);

        assertThrows(
                IllegalArgumentException.class, () -> vacancyService.updateVacancy(vacancyModel));
        verify(vacancyValidator).validateVacancyForUpdate(vacancyModel);
    }

    @Test
    void deleteVacancy_whenExists_thenTrueReturned() {
        Long id = 1L;
        doNothing().when(vacancyValidator).validateVacancyForDelete(id);
        when(vacancyRepository.deleteVacancy(id)).thenReturn(true);

        Boolean result = vacancyService.deleteVacancy(id);

        assertTrue(result);
        verify(vacancyValidator).validateVacancyForDelete(id);
        verify(vacancyRepository).deleteVacancy(id);
        verifyNoMoreInteractions(vacancyRepository);
    }

    @Test
    void deleteVacancy_whenRepositoryReturnsFalse_thenFalseReturned() {
        Long id = 404L;
        doNothing().when(vacancyValidator).validateVacancyForDelete(id);
        when(vacancyRepository.deleteVacancy(id)).thenReturn(false);

        Boolean result = vacancyService.deleteVacancy(id);

        assertFalse(result);
        verify(vacancyValidator).validateVacancyForDelete(id);
        verify(vacancyRepository).deleteVacancy(id);
        verifyNoMoreInteractions(vacancyRepository);
    }
}
