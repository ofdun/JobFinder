package com.ofdun.jobfinder.features.resume.domain.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

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
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
@org.junit.jupiter.api.Tag("equivalence")
class BasicResumeServiceTest {
    @Test
    @org.junit.jupiter.api.Tag("equivalence")
    void createResume_withRealHandlers_usesVectorCreateAfterEmbedding() {
        var ai = mock(com.ofdun.jobfinder.features.clients.ai.AiClient.class);
        var service =
                new BasicResumeService(
                        relationalResumeRepository,
                        vectorResumeRepository,
                        new EmbeddingResumeHandler(ai),
                        new RelationalResumeSaveHandler(relationalResumeRepository),
                        new VectorResumeSaveHandler(vectorResumeRepository),
                        new RelationalResumeUpdateHandler(relationalResumeRepository),
                        new VectorResumeUpdateHandler(vectorResumeRepository),
                        new RelationalResumeGetHandler(relationalResumeRepository),
                        new VectorResumeGetHandler(vectorResumeRepository));
        var model = com.ofdun.jobfinder.support.TestDataMother.resume();
        when(relationalResumeRepository.createResume(model)).thenReturn(42L);
        when(ai.getEmbedding(anyString())).thenReturn(java.util.List.of(0.7f, 0.8f));
        when(vectorResumeRepository.createResume(model)).thenReturn(42L);

        var result = service.createResume(model);

        assertEquals(42L, result);
        assertEquals(java.util.List.of(0.7f, 0.8f), model.getEmbedding());
        var order = inOrder(relationalResumeRepository, ai, vectorResumeRepository);
        order.verify(relationalResumeRepository).createResume(model);
        order.verify(ai).getEmbedding(anyString());
        order.verify(vectorResumeRepository).createResume(model);
        verify(vectorResumeRepository, never()).updateResume(any());
    }

    @Test
    @org.junit.jupiter.api.Tag("equivalence")
    void createResume_whenChainReturnsEmpty_throwsFailure() {
        var model = com.ofdun.jobfinder.support.TestDataMother.resume();
        when(relationalResumeSaveHandler.handle(model)).thenReturn(Optional.empty());

        var error =
                assertThrows(
                        com.ofdun.jobfinder.features.resume.exception.FailedToCreateResumeException
                                .class,
                        () -> resumeService.createResume(model));

        assertNotNull(error);
    }

    @Test
    @org.junit.jupiter.api.Tag("equivalence")
    void getResumeById_whenMissing_returnsEmpty() {
        when(relationalResumeGetHandler.handle(any(ResumeModel.class)))
                .thenReturn(Optional.empty());

        var result = resumeService.getResumeById(404L);

        assertTrue(result.isEmpty());
    }

    @Test
    @org.junit.jupiter.api.Tag("equivalence")
    void updateResume_whenChainFails_propagatesFailure() {
        var model = com.ofdun.jobfinder.support.TestDataMother.resume();
        when(relationalResumeUpdateHandler.handle(model))
                .thenThrow(new IllegalStateException("update failed"));

        var error =
                assertThrows(IllegalStateException.class, () -> resumeService.updateResume(model));

        assertEquals("update failed", error.getMessage());
    }

    @Test
    @org.junit.jupiter.api.Tag("equivalence")
    void searchResumes_whenDefaults_returnsPage() {
        var expected =
                new com.ofdun.jobfinder.common.domain.model.PageResult<ResumeModel>(
                        java.util.List.of(com.ofdun.jobfinder.support.TestDataMother.resume()),
                        0,
                        20,
                        1L,
                        1);
        when(relationalResumeRepository.searchResumes(
                        isNull(), anyInt(), eq(0), any(), anyBoolean()))
                .thenReturn(expected);

        var result = resumeService.searchResumes(null, null);

        assertSame(expected, result);
    }

    @Test
    @org.junit.jupiter.api.Tag("equivalence")
    void searchResumes_whenStorageFails_propagatesFailure() {
        when(relationalResumeRepository.searchResumes(
                        isNull(), anyInt(), anyInt(), any(), anyBoolean()))
                .thenThrow(new IllegalStateException("offline"));

        var error =
                assertThrows(
                        IllegalStateException.class, () -> resumeService.searchResumes(null, null));

        assertEquals("offline", error.getMessage());
    }

    @org.junit.jupiter.params.ParameterizedTest
    @org.junit.jupiter.api.Tag("boundary")
    @org.junit.jupiter.params.provider.CsvSource({
        "0,-1,1,0",
        "1,0,1,0",
        "100,1,100,1",
        "101,10,100,10"
    })
    void searchResumes_clampsPagination(
            int limit, int offset, int expectedLimit, int expectedOffset) {
        var pagination =
                com.ofdun.jobfinder.common.domain.model.OffsetPagination.builder()
                        .limit(limit)
                        .offset(offset)
                        .sortBy("id")
                        .sortDesc(true)
                        .build();

        resumeService.searchResumes(null, pagination);

        verify(relationalResumeRepository)
                .searchResumes(null, expectedLimit, expectedOffset, "id", true);
    }

    @Mock private RelationalResumeRepository relationalResumeRepository;

    @Mock private VectorResumeRepository vectorResumeRepository;

    @Mock private EmbeddingResumeHandler embeddingResumeHandler;

    @Mock private RelationalResumeSaveHandler relationalResumeSaveHandler;

    @Mock private VectorResumeSaveHandler vectorResumeSaveHandler;

    @Mock private RelationalResumeUpdateHandler relationalResumeUpdateHandler;

    @Mock private VectorResumeUpdateHandler vectorResumeUpdateHandler;

    @Mock private RelationalResumeGetHandler relationalResumeGetHandler;

    @Mock private VectorResumeGetHandler vectorResumeGetHandler;

    private BasicResumeService resumeService;

    @BeforeEach
    void setUp() {
        resumeService =
                new BasicResumeService(
                        relationalResumeRepository,
                        vectorResumeRepository,
                        embeddingResumeHandler,
                        relationalResumeSaveHandler,
                        vectorResumeSaveHandler,
                        relationalResumeUpdateHandler,
                        vectorResumeUpdateHandler,
                        relationalResumeGetHandler,
                        vectorResumeGetHandler);
    }

    @Test
    void createResume_whenValidModel_thenIdReturned() {
        ResumeModel inputModel = mock(ResumeModel.class);
        ResumeModel finalModel = mock(ResumeModel.class);
        Long expectedId = 1L;
        when(finalModel.getId()).thenReturn(expectedId);
        when(relationalResumeSaveHandler.handle(inputModel)).thenReturn(Optional.of(finalModel));
        when(embeddingResumeHandler.handle(finalModel)).thenReturn(Optional.of(finalModel));
        when(vectorResumeSaveHandler.handle(finalModel)).thenReturn(Optional.of(finalModel));

        Long actualId = resumeService.createResume(inputModel);

        assertEquals(expectedId, actualId);
        verify(relationalResumeSaveHandler, times(1)).handle(inputModel);
    }

    @Test
    void getResumeById_whenExists_thenResumeReturned() {
        Long id = 1L;
        ResumeModel mockResponse = mock(ResumeModel.class);
        when(relationalResumeGetHandler.handle(any(ResumeModel.class)))
                .thenReturn(Optional.of(mockResponse));
        when(vectorResumeGetHandler.handle(mockResponse)).thenReturn(Optional.of(mockResponse));

        var actualResumeOpt = resumeService.getResumeById(id);

        assertTrue(actualResumeOpt.isPresent());
        assertEquals(mockResponse, actualResumeOpt.get());
        verify(relationalResumeGetHandler, times(1)).handle(any(ResumeModel.class));
    }

    @Test
    void updateResume_whenValidModel_thenUpdatedModelReturned() {
        ResumeModel inputModel = mock(ResumeModel.class);
        ResumeModel updatedModel = mock(ResumeModel.class);
        when(relationalResumeUpdateHandler.handle(inputModel))
                .thenReturn(Optional.of(updatedModel));
        when(embeddingResumeHandler.handle(updatedModel)).thenReturn(Optional.of(updatedModel));
        when(vectorResumeUpdateHandler.handle(updatedModel)).thenReturn(Optional.of(updatedModel));

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

        Exception exception =
                assertThrows(RuntimeException.class, () -> resumeService.deleteResume(id));
        assertEquals("Failed to delete resume from relational repository", exception.getMessage());

        verify(relationalResumeRepository, times(1)).deleteResume(id);
        verify(vectorResumeRepository, never()).deleteResume(id);
    }

    @Test
    void deleteResume_whenVectorDeleteFails_thenThrowsException() {
        Long id = 1L;
        when(relationalResumeRepository.deleteResume(id)).thenReturn(true);
        when(vectorResumeRepository.deleteResume(id)).thenReturn(false);

        Exception exception =
                assertThrows(RuntimeException.class, () -> resumeService.deleteResume(id));
        assertEquals("Failed to delete resume from both repositories", exception.getMessage());

        verify(relationalResumeRepository, times(1)).deleteResume(id);
        verify(vectorResumeRepository, times(1)).deleteResume(id);
    }
}
