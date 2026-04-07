package com.ofdun.jobfinder.features.matching.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import com.ofdun.jobfinder.features.clients.ai.AiClient;
import com.ofdun.jobfinder.features.resume.domain.repository.VectorResumeRepository;
import com.ofdun.jobfinder.features.vacancy.domain.model.VacancyModel;
import com.ofdun.jobfinder.features.vacancy.domain.repository.VacancyRepository;
import com.ofdun.jobfinder.shared.matching.model.MatchResultModel;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class BasicMatchingServiceTest {

    @Mock private VacancyRepository vacancyRepository;

    @Mock private VectorResumeRepository vectorResumeRepository;

    @Mock private AiClient aiClient;

    @InjectMocks private BasicMatchingService matchingService;

    @Test
    void findSuitableCandidates_whenMaxAmountIsZeroOrLess_thenEmptyListReturned() {
        Long vacancyId = 1L;
        Integer maxAmount = 0;

        List<MatchResultModel> result =
                matchingService.findSuitableCandidates(vacancyId, maxAmount);

        assertTrue(result.isEmpty());
        verifyNoInteractions(vacancyRepository, vectorResumeRepository, aiClient);
    }

    @Test
    void findSuitableCandidates_whenMaxAmountIsNegative_thenEmptyListReturned() {
        Long vacancyId = 1L;
        Integer maxAmount = -1;

        List<MatchResultModel> result =
                matchingService.findSuitableCandidates(vacancyId, maxAmount);

        assertTrue(result.isEmpty());
        verifyNoInteractions(vacancyRepository, vectorResumeRepository, aiClient);
    }

    @Test
    void findSuitableCandidates_whenVacancyRepositoryReturnsNull_thenThrowsException() {
        Long vacancyId = 1L;
        Integer maxAmount = 5;
        when(vacancyRepository.getVacancyById(vacancyId)).thenReturn(null);

        assertThrows(
                NullPointerException.class,
                () -> matchingService.findSuitableCandidates(vacancyId, maxAmount));
        verify(vacancyRepository).getVacancyById(vacancyId);
        verifyNoInteractions(aiClient, vectorResumeRepository);
    }

    @Test
    void findSuitableCandidates_whenValidRequest_thenReturnMatchResults() {
        Long vacancyId = 1L;
        Integer maxAmount = 5;
        VacancyModel mockVacancy = mock(VacancyModel.class);
        List<Float> mockEmbedding = List.of(0.1f, 0.2f, 0.3f);
        List<MatchResultModel> expectedResults = List.of(mock(MatchResultModel.class));

        when(vacancyRepository.getVacancyById(vacancyId)).thenReturn(mockVacancy);
        when(mockVacancy.toString()).thenReturn("vacancyString");
        when(aiClient.getEmbedding("vacancyString")).thenReturn(mockEmbedding);
        when(vectorResumeRepository.getMostSimilarResumes(mockEmbedding, maxAmount))
                .thenReturn(expectedResults);

        List<MatchResultModel> actualResults =
                matchingService.findSuitableCandidates(vacancyId, maxAmount);

        assertEquals(expectedResults, actualResults);
        verify(vacancyRepository).getVacancyById(vacancyId);
        verify(aiClient).getEmbedding("vacancyString");
        verify(vectorResumeRepository).getMostSimilarResumes(mockEmbedding, maxAmount);
        verifyNoMoreInteractions(vacancyRepository, aiClient, vectorResumeRepository);
    }
}
