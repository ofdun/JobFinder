package com.ofdun.jobfinder.features.resume.domain.service;

import com.ofdun.jobfinder.features.resume.domain.chain.EmbeddingResumeHandler;
import com.ofdun.jobfinder.features.resume.domain.chain.get.RelationalResumeGetHandler;
import com.ofdun.jobfinder.features.resume.domain.chain.get.VectorResumeGetHandler;
import com.ofdun.jobfinder.features.resume.domain.chain.save.RelationalResumeSaveHandler;
import com.ofdun.jobfinder.features.resume.domain.chain.save.VectorResumeSaveHandler;
import com.ofdun.jobfinder.features.resume.domain.chain.update.RelationalResumeUpdateHandler;
import com.ofdun.jobfinder.features.resume.domain.chain.update.VectorResumeUpdateHandler;
import com.ofdun.jobfinder.features.resume.domain.model.ResumeModel;
import com.ofdun.jobfinder.features.resume.domain.repository.RelationalResumeRepository;
import com.ofdun.jobfinder.features.resume.domain.repository.VectorResumeRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class BasicResumeServiceTest {

    @Mock
    private RelationalResumeRepository relationalResumeRepository;

    @Mock
    private VectorResumeRepository vectorResumeRepository;

    @Mock
    private EmbeddingResumeHandler embeddingResumeHandler;

    @Mock
    private RelationalResumeSaveHandler relationalResumeSaveHandler;

    @Mock
    private VectorResumeSaveHandler vectorResumeSaveHandler;

    @Mock
    private RelationalResumeUpdateHandler relationalResumeUpdateHandler;

    @Mock
    private VectorResumeUpdateHandler vectorResumeUpdateHandler;

    @Mock
    private RelationalResumeGetHandler relationalResumeGetHandler;

    @Mock
    private VectorResumeGetHandler vectorResumeGetHandler;

    private BasicResumeService resumeService;

    @BeforeEach
    void setUp() {
        when(relationalResumeSaveHandler.setNext(embeddingResumeHandler)).thenReturn(embeddingResumeHandler);
        when(embeddingResumeHandler.setNext(vectorResumeSaveHandler)).thenReturn(vectorResumeSaveHandler);

        when(relationalResumeUpdateHandler.setNext(embeddingResumeHandler)).thenReturn(embeddingResumeHandler);
        when(embeddingResumeHandler.setNext(vectorResumeUpdateHandler)).thenReturn(vectorResumeUpdateHandler);

        when(relationalResumeGetHandler.setNext(vectorResumeGetHandler)).thenReturn(vectorResumeGetHandler);

        resumeService = new BasicResumeService(
                relationalResumeRepository,
                vectorResumeRepository,
                embeddingResumeHandler,
                relationalResumeSaveHandler,
                vectorResumeSaveHandler,
                relationalResumeUpdateHandler,
                vectorResumeUpdateHandler,
                relationalResumeGetHandler,
                vectorResumeGetHandler
        );
    }

    @Test
    void createResume_whenValidModel_thenIdReturned() {
        ResumeModel inputModel = mock(ResumeModel.class);
        ResumeModel finalModel = mock(ResumeModel.class);
        Long expectedId = 1L;
        when(finalModel.getId()).thenReturn(expectedId);
        when(relationalResumeSaveHandler.handle(inputModel)).thenReturn(Optional.of(finalModel));

        Long actualId = resumeService.createResume(inputModel);

        assertEquals(expectedId, actualId);
        verify(relationalResumeSaveHandler, times(1)).handle(inputModel);
    }

    @Test
    void getResumeById_whenExists_thenResumeReturned() {
        Long id = 1L;
        ResumeModel mockResponse = mock(ResumeModel.class);
        when(relationalResumeGetHandler.handle(any(ResumeModel.class))).thenReturn(Optional.of(mockResponse));

        var actualResumeOpt = resumeService.getResumeById(id);

        assertTrue(actualResumeOpt.isPresent());
        assertEquals(mockResponse, actualResumeOpt.get());
        verify(relationalResumeGetHandler, times(1)).handle(any(ResumeModel.class));
    }

    @Test
    void updateResume_whenValidModel_thenUpdatedModelReturned() {
        ResumeModel inputModel = mock(ResumeModel.class);
        ResumeModel updatedModel = mock(ResumeModel.class);
        when(relationalResumeUpdateHandler.handle(inputModel)).thenReturn(Optional.of(updatedModel));

        var actualOpt = resumeService.updateResume(inputModel);

        assertTrue(actualOpt.isPresent());
        assertEquals(updatedModel, actualOpt.get());
        verify(relationalResumeUpdateHandler, times(1)).handle(inputModel);
    }

    @Test
    void deleteResume_whenSuccessfullyDeletedFromBothRepos_thenTrueReturned() {
        Long id = 1L;
        when(relationalResumeRepository.deleteResume(id)).thenReturn(true);
        when(vectorResumeRepository.deleteResume(id)).thenReturn(true);

        Boolean result = resumeService.deleteResume(id);

        assertTrue(result);
        verify(relationalResumeRepository, times(1)).deleteResume(id);
        verify(vectorResumeRepository, times(1)).deleteResume(id);
    }

    @Test
    void deleteResume_whenRelationalDeleteFails_thenThrowsExceptionAndSkipsVectorDelete() {
        Long id = 1L;
        when(relationalResumeRepository.deleteResume(id)).thenReturn(false);

        Exception exception = assertThrows(RuntimeException.class, () -> resumeService.deleteResume(id));
        assertEquals("Failed to delete resume from relational repository", exception.getMessage());

        verify(relationalResumeRepository, times(1)).deleteResume(id);
        verify(vectorResumeRepository, never()).deleteResume(id);
    }

    @Test
    void deleteResume_whenVectorDeleteFails_thenThrowsException() {
        Long id = 1L;
        when(relationalResumeRepository.deleteResume(id)).thenReturn(true);
        when(vectorResumeRepository.deleteResume(id)).thenReturn(false);

        Exception exception = assertThrows(RuntimeException.class, () -> resumeService.deleteResume(id));
        assertEquals("Failed to delete resume from both repositories", exception.getMessage());

        verify(relationalResumeRepository, times(1)).deleteResume(id);
        verify(vectorResumeRepository, times(1)).deleteResume(id);
    }
}
