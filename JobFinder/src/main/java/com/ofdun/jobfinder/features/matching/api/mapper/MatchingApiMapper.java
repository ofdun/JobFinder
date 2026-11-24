package com.ofdun.jobfinder.features.matching.api.mapper;

import com.ofdun.jobfinder.features.matching.api.dto.MatchResultResponse;
import com.ofdun.jobfinder.features.matching.domain.model.MatchResultModel;
import io.micrometer.observation.annotation.Observed;
import java.util.List;
import java.util.stream.Collectors;
import org.springframework.stereotype.Component;

@Component
public class MatchingApiMapper {

    @Observed(
            name = "jobfinder.matching.mapper.toResponse",
            contextualName = "Подготовка ответа API для результата подбора",
            lowCardinalityKeyValues = {"feature", "matching", "layer", "mapper"})
    public MatchResultResponse toResponse(MatchResultModel model) {
        if (model == null) {
            return null;
        }
        return new MatchResultResponse(model.getId(), model.getScore());
    }

    @Observed(
            name = "jobfinder.matching.mapper.toResponseList",
            contextualName = "Подготовка ответа API для результата подбора",
            lowCardinalityKeyValues = {"feature", "matching", "layer", "mapper"})
    public List<MatchResultResponse> toResponseList(List<MatchResultModel> models) {
        if (models == null) {
            return null;
        }
        return models.stream().map(this::toResponse).collect(Collectors.toList());
    }
}
