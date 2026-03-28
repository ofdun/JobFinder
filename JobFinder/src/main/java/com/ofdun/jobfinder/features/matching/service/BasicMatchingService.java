package com.ofdun.jobfinder.features.matching.service;

import com.ofdun.jobfinder.features.clients.ai.AiClient;
import com.ofdun.jobfinder.shared.domain.model.MatchResultModel;
import com.ofdun.jobfinder.features.resume.domain.repository.VectorResumeRepository;
import com.ofdun.jobfinder.features.vacancy.domain.repository.VacancyRepository;
import lombok.NonNull;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.List;

@Service
public class BasicMatchingService implements MatchingService {
    private final VacancyRepository vacancyRepository;
    private final VectorResumeRepository vectorResumeRepository;

    public BasicMatchingService(@NonNull VacancyRepository vacancyRepository,
                                @NonNull VectorResumeRepository vectorResumeRepository,
                                @NonNull AiClient aiClient) {
        this.vacancyRepository = vacancyRepository;
        this.vectorResumeRepository = vectorResumeRepository;
        this.aiClient = aiClient;
    }

    private final AiClient aiClient;

    @Override
    public List<MatchResultModel> findSuitableCandidates(@NonNull Long vacancyId, @NonNull Integer maxAmount) {
        if (maxAmount <= 0) {
            return Collections.emptyList();
        }

        var vacancyEmbedding = getVacancyEmbedding(vacancyId);

        return vectorResumeRepository.getMostSimilarResumes(vacancyEmbedding, maxAmount);
    }

    private List<Double> getVacancyEmbedding(@NonNull Long vacancyId) {
        var vacancy = vacancyRepository.getVacancyById(vacancyId);
        return aiClient.getEmbedding(vacancy.toString());
    }
}
