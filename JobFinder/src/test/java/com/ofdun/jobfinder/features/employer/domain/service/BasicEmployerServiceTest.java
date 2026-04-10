package com.ofdun.jobfinder.features.employer.domain.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import com.ofdun.jobfinder.features.employer.domain.model.EmployerModel;
import com.ofdun.jobfinder.features.employer.domain.repository.EmployerRepository;
import com.ofdun.jobfinder.features.employer.domain.validator.EmployerValidator;
import com.ofdun.jobfinder.features.employer.exception.EmployerNotFoundException;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class BasicEmployerServiceTest {

    @Mock private EmployerRepository employerRepository;

    @Mock private EmployerValidator employerValidator;

    @InjectMocks private BasicEmployerService employerService;

    @Test
    void createEmployer_whenValidEmployer_thenIdReturned() {
        EmployerModel employerModel = mock(EmployerModel.class);
        Long expectedId = 1L;
        doNothing().when(employerValidator).validateEmployerForCreate(employerModel);
        when(employerRepository.createEmployer(employerModel)).thenReturn(expectedId);

        Long actualId = employerService.createEmployer(employerModel);

        assertEquals(expectedId, actualId);
        verify(employerValidator).validateEmployerForCreate(employerModel);
        verify(employerRepository).createEmployer(employerModel);
    }

    @Test
    void createEmployer_whenRepositoryReturnsNull_thenNullReturned() {
        EmployerModel employerModel = mock(EmployerModel.class);
        doNothing().when(employerValidator).validateEmployerForCreate(employerModel);
        when(employerRepository.createEmployer(employerModel)).thenReturn(null);

        Long actualId = employerService.createEmployer(employerModel);

        assertNull(actualId);
        verify(employerValidator).validateEmployerForCreate(employerModel);
        verify(employerRepository).createEmployer(employerModel);
    }

    @Test
    void getEmployerById_whenExists_thenEmployerReturned() {
        Long id = 1L;
        EmployerModel expectedEmployer = mock(EmployerModel.class);
        when(employerRepository.getEmployerById(id)).thenReturn(Optional.of(expectedEmployer));

        EmployerModel actualEmployer = employerService.getEmployerById(id);

        assertSame(expectedEmployer, actualEmployer);
        verify(employerRepository).getEmployerById(id);
        verifyNoMoreInteractions(employerRepository);
    }

    @Test
    void getEmployerById_whenMissing_thenThrowsNotFound() {
        Long id = 404L;
        when(employerRepository.getEmployerById(id)).thenReturn(Optional.empty());

        assertThrows(EmployerNotFoundException.class, () -> employerService.getEmployerById(id));

        verify(employerRepository).getEmployerById(id);
        verifyNoMoreInteractions(employerRepository);
    }

    @Test
    void updateEmployer_whenValidEmployer_thenRepositoryCalledAndModelReturned() {
        EmployerModel employerModel = mock(EmployerModel.class);
        doNothing().when(employerValidator).validateEmployerForUpdate(employerModel);
        when(employerRepository.updateEmployer(employerModel)).thenReturn(employerModel);

        EmployerModel actual = employerService.updateEmployer(employerModel);

        assertSame(employerModel, actual);
        verify(employerValidator).validateEmployerForUpdate(employerModel);
        verify(employerRepository).updateEmployer(employerModel);
    }

    @Test
    void updateEmployer_whenInvalidEmployer_thenThrowsIllegalArgumentException() {
        EmployerModel employerModel = mock(EmployerModel.class);
        doThrow(new IllegalArgumentException())
                .when(employerValidator)
                .validateEmployerForUpdate(employerModel);

        assertThrows(
                IllegalArgumentException.class,
                () -> employerService.updateEmployer(employerModel));
        verify(employerValidator).validateEmployerForUpdate(employerModel);
    }

    @Test
    void deleteEmployer_whenExists_thenTrueReturned() {
        Long id = 1L;
        doNothing().when(employerValidator).validateEmployerForDelete(id);
        when(employerRepository.deleteEmployer(id)).thenReturn(true);

        Boolean result = employerService.deleteEmployer(id);

        assertTrue(result);
        verify(employerValidator).validateEmployerForDelete(id);
        verify(employerRepository).deleteEmployer(id);
        verifyNoMoreInteractions(employerRepository);
    }

    @Test
    void deleteEmployer_whenRepositoryReturnsFalse_thenFalseReturned() {
        Long id = 404L;
        doNothing().when(employerValidator).validateEmployerForDelete(id);
        when(employerRepository.deleteEmployer(id)).thenReturn(false);

        Boolean result = employerService.deleteEmployer(id);

        assertFalse(result);
        verify(employerValidator).validateEmployerForDelete(id);
        verify(employerRepository).deleteEmployer(id);
        verifyNoMoreInteractions(employerRepository);
    }
}
