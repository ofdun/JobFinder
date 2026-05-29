package com.ofdun.jobfinder.features.resume.data.mongo.mapper;

import com.ofdun.jobfinder.features.resume.data.mongo.entity.ResumeDocument;
import com.ofdun.jobfinder.features.resume.domain.model.ResumeModel;

public class ResumeMongoMapper {
    public static ResumeDocument toEntity(ResumeModel resumeModel) {
        if (resumeModel == null) {
            return null;
        }
        return new ResumeDocument(
                resumeModel.getId(),
                resumeModel.getApplicantId(),
                resumeModel.getCategoryId(),
                resumeModel.getDescription(),
                resumeModel.getSkillIds(),
                resumeModel.getLanguageIds(),
                resumeModel.getDate());
    }

    public static ResumeModel toModel(ResumeDocument entity) {
        if (entity == null) {
            return null;
        }
        return new ResumeModel(
                entity.getId(),
                entity.getApplicantId(),
                entity.getCategoryId(),
                entity.getDescription(),
                entity.getSkillIds(),
                null,
                null,
                entity.getLanguages(),
                entity.getCreationDate());
    }
}

