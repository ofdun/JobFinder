package com.ofdun.jobfinder.features.applicant.domain.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import com.ofdun.jobfinder.features.applicant.domain.model.ApplicantModel;
import com.ofdun.jobfinder.features.applicant.domain.repository.ApplicantRepository;
import com.ofdun.jobfinder.features.applicant.domain.validator.ApplicantValidator;
import java.util.Optional;

import com.ofdun.jobfinder.features.applicant.exception.ApplicantNotFoundException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class BasicApplicantServiceTest {

    @Mock private ApplicantRepository applicantRepository;

    @Mock private ApplicantValidator applicantValidator;

    @InjectMocks private BasicApplicantService applicantService;

    @Test
    void createApplicant_whenValidApplicant_thenIdReturned() {
        ApplicantModel applicantModel = mock(ApplicantModel.class);
        Long expectedId = 1L;
        doNothing().when(applicantValidator).validateApplicantForCreate(applicantModel);
        when(applicantRepository.createApplicant(applicantModel)).thenReturn(expectedId);

        Long actualId = applicantService.createApplicant(applicantModel);

        assertEquals(expectedId, actualId);
        verify(applicantValidator).validateApplicantForCreate(applicantModel);
        verify(applicantRepository).createApplicant(applicantModel);
    }

    @Test
    void createApplicant_whenRepositoryReturnsNull_thenNullReturned() {
        ApplicantModel applicantModel = mock(ApplicantModel.class);
        doNothing().when(applicantValidator).validateApplicantForCreate(applicantModel);
        when(applicantRepository.createApplicant(applicantModel)).thenReturn(null);

        Long actualId = applicantService.createApplicant(applicantModel);

        assertNull(actualId);
        verify(applicantValidator).validateApplicantForCreate(applicantModel);
        verify(applicantRepository).createApplicant(applicantModel);
    }

    @Test
    void getApplicantById_whenExists_thenApplicantReturned() {
        Long id = 1L;
        ApplicantModel expectedApplicant = mock(ApplicantModel.class);
        when(applicantRepository.getApplicantById(id)).thenReturn(Optional.of(expectedApplicant));

        ApplicantModel actualApplicant = applicantService.getApplicantById(id);

        assertSame(expectedApplicant, actualApplicant);
        verify(applicantRepository).getApplicantById(id);
        verifyNoMoreInteractions(applicantRepository);
    }

    @Test
    void getApplicantById_whenMissing_thenThrowsNotFound() {
        Long id = 404L;
        when(applicantRepository.getApplicantById(id)).thenReturn(Optional.empty());

        assertThrows(ApplicantNotFoundException.class, () -> applicantService.getApplicantById(id));

        verify(applicantRepository).getApplicantById(id);
        verifyNoMoreInteractions(applicantRepository);
    }

    @Test
    void updateApplicant_whenValidApplicant_thenRepositoryCalledAndModelReturned() {
        ApplicantModel applicantModel = mock(ApplicantModel.class);
        doNothing().when(applicantValidator).validateApplicantForUpdate(applicantModel);
        when(applicantRepository.updateApplicant(applicantModel)).thenReturn(applicantModel);

        ApplicantModel actual = applicantService.updateApplicant(applicantModel);

        assertSame(applicantModel, actual);
        verify(applicantValidator).validateApplicantForUpdate(applicantModel);
        verify(applicantRepository).updateApplicant(applicantModel);
    }

    @Test
    void updateApplicant_whenInvalidApplicant_thenThrowsIllegalArgumentException() {
        ApplicantModel applicantModel = mock(ApplicantModel.class);
        doThrow(new IllegalArgumentException())
                .when(applicantValidator)
                .validateApplicantForUpdate(applicantModel);

        assertThrows(
                IllegalArgumentException.class,
                () -> applicantService.updateApplicant(applicantModel));
        verify(applicantValidator).validateApplicantForUpdate(applicantModel);
    }

    @Test
    void deleteApplicant_whenExists_thenTrueReturned() {
        Long id = 1L;
        doNothing().when(applicantValidator).validateApplicantForDelete(id);
        when(applicantRepository.deleteApplicant(id)).thenReturn(true);

        Boolean result = applicantService.deleteApplicant(id);

        assertTrue(result);
        verify(applicantValidator).validateApplicantForDelete(id);
        verify(applicantRepository).deleteApplicant(id);
        verifyNoMoreInteractions(applicantRepository);
    }

    @Test
    void deleteApplicant_whenRepositoryReturnsFalse_thenFalseReturned() {
        Long id = 404L;
        doNothing().when(applicantValidator).validateApplicantForDelete(id);
        when(applicantRepository.deleteApplicant(id)).thenReturn(false);

        Boolean result = applicantService.deleteApplicant(id);

        assertFalse(result);
        verify(applicantValidator).validateApplicantForDelete(id);
        verify(applicantRepository).deleteApplicant(id);
        verifyNoMoreInteractions(applicantRepository);
    }
}
