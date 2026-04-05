package com.ofdun.jobfinder.shared.experience.mapper;

import com.ofdun.jobfinder.shared.experience.entity.JobExperienceEntity;
import com.ofdun.jobfinder.shared.experience.model.JobExperienceModel;

public class JobExperienceMapper {
    public static JobExperienceModel toModel(JobExperienceEntity entity) {
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

    public static JobExperienceEntity toEntity(JobExperienceModel model) {
        if (model == null) {
            return null;
        }
        return new JobExperienceEntity(
                model.getId(),
                model.getResumeId(),
                model.getPosition(),
                model.getCompanyName(),
                model.getDescription(),
                model.getStartDate(),
                model.getEndDate());
    }
}
