package com.ofdun.jobfinder.features.experience.data.mongo.mapper;

import com.ofdun.jobfinder.features.experience.data.mongo.entity.JobExperienceDocument;
import com.ofdun.jobfinder.features.experience.domain.model.JobExperienceModel;

public class JobExperienceMongoMapper {
    public static JobExperienceModel toModel(JobExperienceDocument entity) {
        if (entity == null) {
            return null;
        }
        return new JobExperienceModel(
                entity.getId(),
                entity.getResumeId(),
                entity.getPosition(),
                entity.getCompanyName(),
                entity.getDescription(),
                entity.getStartDate(),
                entity.getEndDate());
    }

    public static JobExperienceDocument toEntity(JobExperienceModel model) {
        if (model == null) {
            return null;
        }
        return new JobExperienceDocument(
                model.getId(),
                model.getResumeId(),
                model.getPosition(),
                model.getCompanyName(),
                model.getDescription(),
                model.getStartDate(),
                model.getEndDate());
    }
}

