package com.ofdun.jobfinder.features.matching.service;

import com.ofdun.jobfinder.shared.domain.model.MatchResultModel;

import java.util.List;

public interface MatchingService {
    List<MatchResultModel> findSuitableCandidates(Long vacancyId, Integer maxAmount);
}
