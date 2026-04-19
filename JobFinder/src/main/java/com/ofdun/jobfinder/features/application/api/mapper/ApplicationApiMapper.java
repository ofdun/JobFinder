package com.ofdun.jobfinder.features.application.api.mapper;

import com.ofdun.jobfinder.features.application.api.dto.ApplicationRequest;
import com.ofdun.jobfinder.features.application.api.dto.ApplicationResponse;
import com.ofdun.jobfinder.features.application.domain.model.ApplicationModel;
import java.util.Date;
import org.springframework.stereotype.Component;

@Component
public class ApplicationApiMapper {

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
