package com.ofdun.jobfinder.features.application.data.mongo.mapper;

import com.ofdun.jobfinder.features.application.data.mongo.entity.ApplicationDocument;
import com.ofdun.jobfinder.features.application.domain.model.ApplicationModel;

public class ApplicationMongoMapper {
    public static ApplicationDocument toEntity(ApplicationModel application) {
        if (application == null) {
            return null;
        }
        return new ApplicationDocument(
                application.getId(),
                application.getVacancyId(),
                application.getResumeId(),
                application.getApplicationDate(),
                application.getApplicationStatus());
    }

    public static ApplicationModel toModel(ApplicationDocument entity) {
        if (entity == null) {
            return null;
        }
        return new ApplicationModel(
                entity.getId(),
                entity.getVacancyId(),
                entity.getResumeId(),
                entity.getApplicationDate(),
                entity.getApplicationStatus());
    }
}

