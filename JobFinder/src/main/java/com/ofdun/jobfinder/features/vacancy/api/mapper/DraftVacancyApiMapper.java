package com.ofdun.jobfinder.features.vacancy.api.mapper;

import com.ofdun.jobfinder.features.vacancy.api.dto.DraftVacancyResponse;
import com.ofdun.jobfinder.features.vacancy.domain.model.DraftVacancyModel;
import io.micrometer.observation.annotation.Observed;
import org.springframework.stereotype.Component;

@Component
public class DraftVacancyApiMapper {
    @Observed(
            name = "jobfinder.vacancy.mapper.toResponse",
            contextualName = "Подготовка ответа API для вакансии",
            lowCardinalityKeyValues = {"feature", "vacancy", "layer", "mapper"})
    public DraftVacancyResponse toResponse(DraftVacancyModel model) {
        if (model == null) {
            return null;
        }
        return new DraftVacancyResponse(
                model.getId(), model.getVacancyId(), model.getVersionTimestamp());
    }
}
