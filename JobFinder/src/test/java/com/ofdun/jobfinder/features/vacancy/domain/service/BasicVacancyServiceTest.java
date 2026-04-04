package com.ofdun.jobfinder.features.vacancy.domain.service;

import com.ofdun.jobfinder.features.vacancy.domain.model.VacancyModel;
import com.ofdun.jobfinder.features.vacancy.domain.repository.VacancyRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class BasicVacancyServiceTest {

    @Mock
    private VacancyRepository vacancyRepository;

    @InjectMocks
    private BasicVacancyService vacancyService;

    @Test
    void createVacancy_whenValidVacancy_thenIdReturned() {
        VacancyModel vacancyModel = mock(VacancyModel.class);
        Long expectedId = 1L;
        when(vacancyRepository.createVacancy(vacancyModel)).thenReturn(expectedId);

        Long actualId = vacancyService.createVacancy(vacancyModel);

        assertEquals(expectedId, actualId);
        verify(vacancyRepository).createVacancy(vacancyModel);
    }

    @Test
    void createVacancy_whenRepositoryReturnsNull_thenNullReturned() {
        VacancyModel vacancyModel = mock(VacancyModel.class);
        when(vacancyRepository.createVacancy(vacancyModel)).thenReturn(null);

        Long actualId = vacancyService.createVacancy(vacancyModel);

        assertNull(actualId);
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
    void updateVacancy_whenValidVacancy_thenThrowsIllegalArgumentException() {
        VacancyModel vacancyModel = mock(VacancyModel.class);
        when(vacancyModel.getId()).thenReturn(1L);

        assertThrows(IllegalArgumentException.class, () -> vacancyService.updateVacancy(vacancyModel));
    }

    @Test
    void updateVacancy_whenInvalidVacancy_thenThrowsIllegalArgumentException() {
        VacancyModel vacancyModel = mock(VacancyModel.class);
        when(vacancyModel.getId()).thenReturn(0L);

        assertThrows(IllegalArgumentException.class, () -> vacancyService.updateVacancy(vacancyModel));
    }

    @Test
    void deleteVacancy_whenExists_thenTrueReturned() {
        Long id = 1L;
        when(vacancyRepository.deleteVacancy(id)).thenReturn(true);

        Boolean result = vacancyService.deleteVacancy(id);

        assertTrue(result);
        verify(vacancyRepository).deleteVacancy(id);
        verifyNoMoreInteractions(vacancyRepository);
    }

    @Test
    void deleteVacancy_whenRepositoryReturnsFalse_thenThrowsIllegalArgumentException() {
        Long id = 404L;
        when(vacancyRepository.deleteVacancy(id)).thenReturn(false);

        assertThrows(IllegalArgumentException.class, () -> vacancyService.deleteVacancy(id));
        verify(vacancyRepository).deleteVacancy(id);
        verifyNoMoreInteractions(vacancyRepository);
    }
}
