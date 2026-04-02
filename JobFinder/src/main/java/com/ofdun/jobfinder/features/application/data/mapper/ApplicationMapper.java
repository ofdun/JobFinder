package com.ofdun.jobfinder.features.application.data.mapper;

import com.ofdun.jobfinder.features.application.data.entity.ApplicationEntity;
import com.ofdun.jobfinder.features.application.domain.model.ApplicationModel;

public class ApplicationMapper {
    public static ApplicationEntity toEntity(ApplicationModel application) {
        return new ApplicationEntity(
                application.getId(),
                application.getVacancyId(),
                application.getResumeId(),
                application.getApplicationDate(),
                application.getApplicationStatus()
        );
    }

    public static ApplicationModel toModel(ApplicationEntity entity) {
        return new ApplicationModel(
                entity.getId(),
                entity.getVacancyId(),
                entity.getResumeId(),
                entity.getApplicationDate(),
                entity.getApplicationStatus()
        );
    }
}
