package com.ofdun.jobfinder.features.applicant.domain.service;

import com.ofdun.jobfinder.features.applicant.domain.model.ApplicantModel;
import com.ofdun.jobfinder.features.applicant.domain.repository.ApplicantRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class BasicApplicantServiceTest {

    @Mock
    private ApplicantRepository applicantRepository;

    @InjectMocks
    private BasicApplicantService applicantService;

    @Test
    void createApplicant_whenValidApplicant_thenIdReturned() {
        ApplicantModel applicantModel = mock(ApplicantModel.class);
        Long expectedId = 1L;
        when(applicantRepository.createApplicant(applicantModel)).thenReturn(expectedId);

        Long actualId = applicantService.createApplicant(applicantModel);

        assertEquals(expectedId, actualId);
        verify(applicantRepository).createApplicant(applicantModel);
        verifyNoMoreInteractions(applicantRepository);
    }

    @Test
    void createApplicant_whenRepositoryReturnsNull_thenNullReturned() {
        ApplicantModel applicantModel = mock(ApplicantModel.class);
        when(applicantRepository.createApplicant(applicantModel)).thenReturn(null);

        Long actualId = applicantService.createApplicant(applicantModel);

        assertNull(actualId);
        verify(applicantRepository).createApplicant(applicantModel);
        verifyNoMoreInteractions(applicantRepository);
    }

    @Test
    void getApplicantById_whenExists_thenApplicantReturned() {
        Long id = 1L;
        ApplicantModel expectedApplicant = mock(ApplicantModel.class);
        when(applicantRepository.getApplicantById(id)).thenReturn(expectedApplicant);

        ApplicantModel actualApplicant = applicantService.getApplicantById(id);

        assertSame(expectedApplicant, actualApplicant);
        verify(applicantRepository).getApplicantById(id);
        verifyNoMoreInteractions(applicantRepository);
    }

    @Test
    void getApplicantById_whenMissing_thenNullReturned() {
        Long id = 404L;
        when(applicantRepository.getApplicantById(id)).thenReturn(null);

        ApplicantModel actualApplicant = applicantService.getApplicantById(id);

        assertNull(actualApplicant);
        verify(applicantRepository).getApplicantById(id);
        verifyNoMoreInteractions(applicantRepository);
    }

    @Test
    void updateApplicant_whenValidApplicant_thenUpdatedApplicantReturned() {
        ApplicantModel applicantModel = mock(ApplicantModel.class);
        when(applicantRepository.updateApplicant(applicantModel)).thenReturn(applicantModel);

        ApplicantModel updatedApplicant = applicantService.updateApplicant(applicantModel);

        assertSame(applicantModel, updatedApplicant);
        verify(applicantRepository).updateApplicant(applicantModel);
        verifyNoMoreInteractions(applicantRepository);
    }

    @Test
    void deleteApplicant_whenExists_thenTrueReturned() {
        Long id = 1L;
        when(applicantRepository.deleteApplicant(id)).thenReturn(true);

        Boolean result = applicantService.deleteApplicant(id);

        assertTrue(result);
        verify(applicantRepository).deleteApplicant(id);
        verifyNoMoreInteractions(applicantRepository);
    }

    @Test
    void deleteApplicant_whenRepositoryReturnsFalse_thenFalseReturned() {
        Long id = 404L;
        when(applicantRepository.deleteApplicant(id)).thenReturn(false);

        Boolean result = applicantService.deleteApplicant(id);

        assertFalse(result);
        verify(applicantRepository).deleteApplicant(id);
        verifyNoMoreInteractions(applicantRepository);
    }
}
