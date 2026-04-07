package com.ofdun.jobfinder.features.vacancy.domain.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import com.ofdun.jobfinder.features.vacancy.domain.model.VacancyModel;
import com.ofdun.jobfinder.features.vacancy.domain.repository.VacancyRepository;
import com.ofdun.jobfinder.features.vacancy.domain.validator.VacancyValidator;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class BasicVacancyServiceTest {

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
        when(vacancyRepository.getVacancyById(id)).thenReturn(expectedVacancy);

        VacancyModel actualVacancy = vacancyService.getVacancyById(id);

        assertSame(expectedVacancy, actualVacancy);
        verify(vacancyRepository).getVacancyById(id);
        verifyNoMoreInteractions(vacancyRepository);
    }

    @Test
    void getVacancyById_whenMissing_thenNullReturned() {
        Long id = 404L;
        when(vacancyRepository.getVacancyById(id)).thenReturn(null);

        VacancyModel actualVacancy = vacancyService.getVacancyById(id);

        assertNull(actualVacancy);
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
