package com.ofdun.jobfinder.features.matching.domain.service;

import com.ofdun.jobfinder.features.matching.domain.model.MatchResultModel;
import java.util.List;

public interface MatchingService {
    List<MatchResultModel> findSuitableCandidates(Long vacancyId, Integer maxAmount);
}
