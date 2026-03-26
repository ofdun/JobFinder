package com.ofdun.jobfinder.features.employer.domain.service;

import com.ofdun.jobfinder.features.employer.domain.model.EmployerModel;
import com.ofdun.jobfinder.features.employer.domain.repository.EmployerRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class BasicEmployerServiceTest {

    @Mock
    private EmployerRepository employerRepository;

    @InjectMocks
    private BasicEmployerService employerService;

    @Test
    void createEmployer_whenValidEmployer_thenIdReturned() {
        EmployerModel employerModel = mock(EmployerModel.class);
        Long expectedId = 1L;
        when(employerRepository.createEmployer(employerModel)).thenReturn(expectedId);

        Long actualId = employerService.createEmployer(employerModel);

        assertEquals(expectedId, actualId);
        verify(employerRepository).createEmployer(employerModel);
        verifyNoMoreInteractions(employerRepository);
    }

    @Test
    void createEmployer_whenRepositoryReturnsNull_thenNullReturned() {
        EmployerModel employerModel = mock(EmployerModel.class);
        when(employerRepository.createEmployer(employerModel)).thenReturn(null);

        Long actualId = employerService.createEmployer(employerModel);

        assertNull(actualId);
        verify(employerRepository).createEmployer(employerModel);
        verifyNoMoreInteractions(employerRepository);
    }

    @Test
    void getEmployerById_whenExists_thenEmployerReturned() {
        Long id = 1L;
        EmployerModel expectedEmployer = mock(EmployerModel.class);
        when(employerRepository.getEmployerById(id)).thenReturn(expectedEmployer);

        EmployerModel actualEmployer = employerService.getEmployerById(id);

        assertSame(expectedEmployer, actualEmployer);
        verify(employerRepository).getEmployerById(id);
        verifyNoMoreInteractions(employerRepository);
    }

    @Test
    void getEmployerById_whenMissing_thenNullReturned() {
        Long id = 404L;
        when(employerRepository.getEmployerById(id)).thenReturn(null);

        EmployerModel actualEmployer = employerService.getEmployerById(id);

        assertNull(actualEmployer);
        verify(employerRepository).getEmployerById(id);
        verifyNoMoreInteractions(employerRepository);
    }

    @Test
    void updateEmployer_whenValidEmployer_thenUpdatedEmployerReturned() {
        EmployerModel employerModel = mock(EmployerModel.class);
        when(employerRepository.updateEmployer(employerModel)).thenReturn(employerModel);

        EmployerModel updatedEmployer = employerService.updateEmployer(employerModel);

        assertSame(employerModel, updatedEmployer);
        verify(employerRepository).updateEmployer(employerModel);
        verifyNoMoreInteractions(employerRepository);
    }

    @Test
    void deleteEmployer_whenExists_thenTrueReturned() {
        Long id = 1L;
        when(employerRepository.deleteEmployer(id)).thenReturn(true);

        Boolean result = employerService.deleteEmployer(id);

        assertTrue(result);
        verify(employerRepository).deleteEmployer(id);
        verifyNoMoreInteractions(employerRepository);
    }

    @Test
    void deleteEmployer_whenRepositoryReturnsFalse_thenFalseReturned() {
        Long id = 404L;
        when(employerRepository.deleteEmployer(id)).thenReturn(false);

        Boolean result = employerService.deleteEmployer(id);

        assertFalse(result);
        verify(employerRepository).deleteEmployer(id);
        verifyNoMoreInteractions(employerRepository);
    }
}
