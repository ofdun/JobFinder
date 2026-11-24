package com.ofdun.jobfinder.features.application.api.mapper;

import com.ofdun.jobfinder.features.application.api.dto.ApplicationRequest;
import com.ofdun.jobfinder.features.application.api.dto.ApplicationResponse;
import com.ofdun.jobfinder.features.application.domain.model.ApplicationModel;
import io.micrometer.observation.annotation.Observed;
import java.util.Date;
import org.springframework.stereotype.Component;

@Component
public class ApplicationApiMapper {

    @Observed(
            name = "jobfinder.application.mapper.toModel",
            contextualName = "Подготовка модели отклика",
            lowCardinalityKeyValues = {"feature", "application", "layer", "mapper"})
    public ApplicationModel toModel(ApplicationRequest request) {
        if (request == null) {
            return null;
        }

        return new ApplicationModel(
                null,
                request.getVacancyId(),
                request.getResumeId(),
                new Date(),
                request.getApplicationStatus());
    }

    @Observed(
            name = "jobfinder.application.mapper.toResponse",
            contextualName = "Подготовка ответа API для отклика",
            lowCardinalityKeyValues = {"feature", "application", "layer", "mapper"})
    public ApplicationResponse toResponse(ApplicationModel model) {
        if (model == null) {
            return null;
        }

        return ApplicationResponse.builder()
                .id(model.getId())
                .vacancyId(model.getVacancyId())
                .resumeId(model.getResumeId())
                .applicationDate(model.getApplicationDate())
                .applicationStatus(model.getApplicationStatus())
                .build();
    }
}
