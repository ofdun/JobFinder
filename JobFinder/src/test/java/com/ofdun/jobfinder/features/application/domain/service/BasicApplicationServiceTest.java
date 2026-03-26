package com.ofdun.jobfinder.features.application.domain.service;

import com.ofdun.jobfinder.features.application.domain.model.ApplicationModel;
import com.ofdun.jobfinder.features.application.domain.repository.ApplicationRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class BasicApplicationServiceTest {

    @Mock
    private ApplicationRepository applicationRepository;

    @InjectMocks
    private BasicApplicationService applicationService;

    @Test
    void saveApplication_whenValidApplication_thenIdReturned() {
        ApplicationModel application = mock(ApplicationModel.class);
        Long expectedId = 1L;
        when(applicationRepository.saveApplication(application)).thenReturn(expectedId);

        Long actualId = applicationService.saveApplication(application);

        assertEquals(expectedId, actualId);
        verify(applicationRepository).saveApplication(application);
        verifyNoMoreInteractions(applicationRepository);
    }

    @Test
    void saveApplication_whenRepositoryReturnsNull_thenNullReturned() {
        ApplicationModel application = mock(ApplicationModel.class);
        when(applicationRepository.saveApplication(application)).thenReturn(null);

        Long actualId = applicationService.saveApplication(application);

        assertNull(actualId);
        verify(applicationRepository).saveApplication(application);
        verifyNoMoreInteractions(applicationRepository);
    }

    @Test
    void getApplication_whenExists_thenApplicationReturned() {
        Long id = 1L;
        ApplicationModel expectedApplication = mock(ApplicationModel.class);
        when(applicationRepository.getApplication(id)).thenReturn(expectedApplication);

        ApplicationModel actualApplication = applicationService.getApplication(id);

        assertSame(expectedApplication, actualApplication);
        verify(applicationRepository).getApplication(id);
        verifyNoMoreInteractions(applicationRepository);
    }

    @Test
    void getApplication_whenMissing_thenNullReturned() {
        Long id = 99L;
        when(applicationRepository.getApplication(id)).thenReturn(null);

        ApplicationModel actualApplication = applicationService.getApplication(id);

        assertNull(actualApplication);
        verify(applicationRepository).getApplication(id);
        verifyNoMoreInteractions(applicationRepository);
    }

    @Test
    void updateApplication_whenValidApplication_thenUpdatedApplicationReturned() {
        ApplicationModel application = mock(ApplicationModel.class);
        when(applicationRepository.updateApplication(application)).thenReturn(application);

        ApplicationModel updatedApplication = applicationService.updateApplication(application);

        assertSame(application, updatedApplication);
        verify(applicationRepository).updateApplication(application);
        verifyNoMoreInteractions(applicationRepository);
    }

    @Test
    void deleteApplication_whenExists_thenTrueReturned() {
        Long id = 1L;
        when(applicationRepository.deleteApplication(id)).thenReturn(true);

        Boolean result = applicationService.deleteApplication(id);

        assertTrue(result);
        verify(applicationRepository).deleteApplication(id);
        verifyNoMoreInteractions(applicationRepository);
    }

    @Test
    void deleteApplication_whenRepositoryReturnsFalse_thenFalseReturned() {
        Long id = 404L;
        when(applicationRepository.deleteApplication(id)).thenReturn(false);

        Boolean result = applicationService.deleteApplication(id);

        assertFalse(result);
        verify(applicationRepository).deleteApplication(id);
        verifyNoMoreInteractions(applicationRepository);
    }
}
