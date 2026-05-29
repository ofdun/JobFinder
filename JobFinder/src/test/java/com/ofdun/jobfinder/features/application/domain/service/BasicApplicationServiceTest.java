package com.ofdun.jobfinder.features.application.domain.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import com.ofdun.jobfinder.features.application.domain.model.ApplicationModel;
import com.ofdun.jobfinder.features.application.domain.repository.ApplicationRepository;
import com.ofdun.jobfinder.features.application.domain.validator.ApplicationValidator;
import com.ofdun.jobfinder.features.application.exception.ApplicationNotFoundException;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class BasicApplicationServiceTest {

    @Mock private ApplicationRepository applicationRepository;

    @Mock private ApplicationValidator applicationValidator;

    @InjectMocks private BasicApplicationService applicationService;

    @Test
    void saveApplication_whenValidApplication_thenIdReturned() {
        ApplicationModel application = mock(ApplicationModel.class);
        Long expectedId = 1L;
        doNothing().when(applicationValidator).validateApplicationForCreate(application);
        when(applicationRepository.createApplication(application)).thenReturn(expectedId);

        Long actualId = applicationService.saveApplication(application);

        assertEquals(expectedId, actualId);
        verify(applicationValidator).validateApplicationForCreate(application);
        verify(applicationRepository).createApplication(application);
    }

    @Test
    void saveApplication_whenRepositoryReturnsNull_thenNullReturned() {
        ApplicationModel application = mock(ApplicationModel.class);
        doNothing().when(applicationValidator).validateApplicationForCreate(application);
        when(applicationRepository.createApplication(application)).thenReturn(null);

        Long actualId = applicationService.saveApplication(application);

        assertNull(actualId);
        verify(applicationValidator).validateApplicationForCreate(application);
        verify(applicationRepository).createApplication(application);
    }

    @Test
    void getApplication_whenExists_thenApplicationReturned() {
        Long id = 1L;
        ApplicationModel expectedApplication = mock(ApplicationModel.class);
        when(applicationRepository.getApplicationById(id))
                .thenReturn(Optional.of(expectedApplication));

        ApplicationModel actualApplication = applicationService.getApplication(id);

        assertSame(expectedApplication, actualApplication);
        verify(applicationRepository).getApplicationById(id);
        verifyNoMoreInteractions(applicationRepository);
    }

    @Test
    void getApplication_whenMissing_thenThrowsNotFound() {
        Long id = 99L;
        when(applicationRepository.getApplicationById(id)).thenReturn(Optional.empty());

        assertThrows(
                ApplicationNotFoundException.class, () -> applicationService.getApplication(id));

        verify(applicationRepository).getApplicationById(id);
        verifyNoMoreInteractions(applicationRepository);
    }

    @Test
    void getApplicationsByVacancyId_whenValidVacancyId_thenApplicationsReturned() {
        Long vacancyId = 10L;
        List<ApplicationModel> expected = List.of(mock(ApplicationModel.class), mock(ApplicationModel.class));
        when(applicationRepository.getApplicationsByVacancyId(vacancyId)).thenReturn(expected);

        List<ApplicationModel> actual = applicationService.getApplicationsByVacancyId(vacancyId);

        assertSame(expected, actual);
        verify(applicationRepository).getApplicationsByVacancyId(vacancyId);
    }

    @Test
    void getApplicationsByVacancyId_whenInvalidVacancyId_thenThrowsIllegalArgumentException() {
        assertThrows(
                IllegalArgumentException.class,
                () -> applicationService.getApplicationsByVacancyId(0L));

        verifyNoInteractions(applicationRepository);
    }

    @Test
    void updateApplication_whenValidApplication_thenRepositoryCalledAndModelReturned() {
        ApplicationModel application = mock(ApplicationModel.class);
        doNothing().when(applicationValidator).validateApplicationForUpdate(application);
        when(applicationRepository.updateApplication(application)).thenReturn(application);

        ApplicationModel actual = applicationService.updateApplication(application);

        assertSame(application, actual);
        verify(applicationValidator).validateApplicationForUpdate(application);
        verify(applicationRepository).updateApplication(application);
    }

    @Test
    void updateApplication_whenInvalidApplication_thenThrowsIllegalArgumentException() {
        ApplicationModel application = mock(ApplicationModel.class);
        doThrow(new IllegalArgumentException())
                .when(applicationValidator)
                .validateApplicationForUpdate(application);

        assertThrows(
                IllegalArgumentException.class,
                () -> applicationService.updateApplication(application));
        verify(applicationValidator).validateApplicationForUpdate(application);
    }

    @Test
    void deleteApplication_whenExists_thenTrueReturned() {
        Long id = 1L;
        doNothing().when(applicationValidator).validateApplicationForDelete(id);
        when(applicationRepository.deleteApplication(id)).thenReturn(true);

        Boolean result = applicationService.deleteApplication(id);

        assertTrue(result);
        verify(applicationValidator).validateApplicationForDelete(id);
        verify(applicationRepository).deleteApplication(id);
        verifyNoMoreInteractions(applicationRepository);
    }

    @Test
    void deleteApplication_whenRepositoryReturnsFalse_thenFalseReturned() {
        Long id = 404L;
        doNothing().when(applicationValidator).validateApplicationForDelete(id);
        when(applicationRepository.deleteApplication(id)).thenReturn(false);

        Boolean result = applicationService.deleteApplication(id);

        assertFalse(result);
        verify(applicationValidator).validateApplicationForDelete(id);
        verify(applicationRepository).deleteApplication(id);
        verifyNoMoreInteractions(applicationRepository);
    }
}
