package com.ofdun.jobfinder.features.resume.data.mapper;

import com.ofdun.jobfinder.features.resume.data.entity.ResumeEntity;
import com.ofdun.jobfinder.features.resume.domain.model.ResumeModel;

public class ResumeMapper {
    public static ResumeModel toModel(ResumeEntity resumeEntity) {
        if (resumeEntity == null) {
            return null;
        }
        return new ResumeModel(
                resumeEntity.getId(),
                resumeEntity.getApplicantId(),
                resumeEntity.getCategoryId(),
                resumeEntity.getDescription(),
                resumeEntity.getSkillIds(),
                null,
                null,
                resumeEntity.getLanguages(),
                resumeEntity.getCreationDate());
    }

    public static ResumeEntity toEntity(ResumeModel resumeModel) {
        if (resumeModel == null) {
            return null;
        }
        return new ResumeEntity(
                resumeModel.getId(),
                resumeModel.getApplicantId(),
                resumeModel.getCategoryId(),
                resumeModel.getDescription(),
                resumeModel.getSkillIds(),
                resumeModel.getLanguageIds(),
                null);
    }
}
