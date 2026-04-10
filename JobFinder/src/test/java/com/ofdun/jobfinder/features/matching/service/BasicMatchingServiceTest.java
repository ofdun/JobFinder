package com.ofdun.jobfinder.features.matching.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import com.ofdun.jobfinder.features.clients.ai.AiClient;
import com.ofdun.jobfinder.features.matching.domain.service.BasicMatchingService;
import com.ofdun.jobfinder.features.resume.domain.repository.VectorResumeRepository;
import com.ofdun.jobfinder.features.vacancy.domain.model.VacancyModel;
import com.ofdun.jobfinder.features.vacancy.domain.repository.VacancyRepository;
import com.ofdun.jobfinder.features.matching.domain.model.MatchResultModel;
import java.util.List;
import java.util.Optional;
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

        List<MatchResultModel> result = matchingService.findSuitableCandidates(vacancyId, maxAmount);

        assertTrue(result.isEmpty());
        verifyNoInteractions(vacancyRepository, vectorResumeRepository, aiClient);
    }

    @Test
    void findSuitableCandidates_whenMaxAmountIsNegative_thenEmptyListReturned() {
        Long vacancyId = 1L;
        Integer maxAmount = -1;

        List<MatchResultModel> result = matchingService.findSuitableCandidates(vacancyId, maxAmount);

        assertTrue(result.isEmpty());
        verifyNoInteractions(vacancyRepository, vectorResumeRepository, aiClient);
    }

    @Test
    void findSuitableCandidates_whenVacancyRepositoryReturnsEmpty_thenReturnEmptyList() {
        Long vacancyId = 1L;
        Integer maxAmount = 5;
        when(vacancyRepository.getVacancyById(vacancyId)).thenReturn(Optional.empty());
        when(aiClient.getEmbedding("Optional.empty")).thenReturn(List.of());
        when(vectorResumeRepository.getMostSimilarResumes(List.of(), maxAmount)).thenReturn(List.of());

        List<MatchResultModel> result = matchingService.findSuitableCandidates(vacancyId, maxAmount);

        assertTrue(result.isEmpty());
        verify(vacancyRepository).getVacancyById(vacancyId);
        verify(aiClient).getEmbedding("Optional.empty");
        verify(vectorResumeRepository).getMostSimilarResumes(List.of(), maxAmount);
        verifyNoMoreInteractions(vacancyRepository, aiClient, vectorResumeRepository);
    }

    @Test
    void findSuitableCandidates_whenValidRequest_thenReturnMatchResults() {
        Long vacancyId = 1L;
        Integer maxAmount = 5;
        VacancyModel mockVacancy = mock(VacancyModel.class);
        List<Float> mockEmbedding = List.of(0.1f, 0.2f, 0.3f);
        List<MatchResultModel> expectedResults = List.of(mock(MatchResultModel.class));

        when(vacancyRepository.getVacancyById(vacancyId)).thenReturn(Optional.of(mockVacancy));
        when(mockVacancy.toString()).thenReturn("vacancyString");
        when(aiClient.getEmbedding("Optional[vacancyString]")).thenReturn(mockEmbedding);
        when(vectorResumeRepository.getMostSimilarResumes(mockEmbedding, maxAmount))
                .thenReturn(expectedResults);

        List<MatchResultModel> actualResults =
                matchingService.findSuitableCandidates(vacancyId, maxAmount);

        assertEquals(expectedResults, actualResults);
        verify(vacancyRepository).getVacancyById(vacancyId);
        verify(aiClient).getEmbedding("Optional[vacancyString]");
        verify(vectorResumeRepository).getMostSimilarResumes(mockEmbedding, maxAmount);
        verifyNoMoreInteractions(vacancyRepository, aiClient, vectorResumeRepository);
    }
}
