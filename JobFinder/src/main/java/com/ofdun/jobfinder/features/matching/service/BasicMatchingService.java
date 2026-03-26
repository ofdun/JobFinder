package com.ofdun.jobfinder.features.matching.service;

import com.ofdun.jobfinder.features.clients.ai.AiClient;
import com.ofdun.jobfinder.shared.domain.model.MatchResultModel;
import com.ofdun.jobfinder.features.resume.domain.repository.VectorResumeRepository;
import com.ofdun.jobfinder.features.vacancy.domain.repository.VacancyRepository;
import lombok.NonNull;

import java.util.Collections;
import java.util.List;

public class BasicMatchingService implements MatchingService {
    VacancyRepository vacancyRepository;
    VectorResumeRepository vectorResumeRepository;
    AiClient aiClient;

    @Override
    @NonNull
    public List<MatchResultModel> findSuitableCandidates(Long vacancyId, Integer maxAmount) {
        if (maxAmount <= 0) {
            return Collections.emptyList();
        }

        var vacancyEmbedding = getVacancyEmbedding(vacancyId);

        return vectorResumeRepository.getMostSimilarResumes(vacancyEmbedding, maxAmount);
    }

    @NonNull
    private List<Double> getVacancyEmbedding(Long vacancyId) {
        var vacancy = vacancyRepository.getVacancyById(vacancyId);
        return aiClient.getEmbedding(vacancy.toString());
    }
}
