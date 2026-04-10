package com.ofdun.jobfinder.features.matching.api.mapper;

import com.ofdun.jobfinder.features.matching.api.dto.MatchResultResponse;
import com.ofdun.jobfinder.features.matching.domain.model.MatchResultModel;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

@Component
public class MatchingApiMapper {

    public MatchResultResponse toResponse(MatchResultModel model) {
        if (model == null) {
            return null;
        }
        return new MatchResultResponse(model.getId(), model.getScore());
    }

    public List<MatchResultResponse> toResponseList(List<MatchResultModel> models) {
        if (models == null) {
            return null;
        }
        return models.stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }
}

