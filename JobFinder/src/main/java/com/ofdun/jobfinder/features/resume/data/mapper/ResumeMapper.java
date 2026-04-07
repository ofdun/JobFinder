package com.ofdun.jobfinder.features.resume.data.mapper;

import com.ofdun.jobfinder.features.resume.data.entity.ResumeEntity;
import com.ofdun.jobfinder.features.resume.domain.model.ResumeModel;
import com.ofdun.jobfinder.shared.category.mapper.CategoryMapper;
import com.ofdun.jobfinder.shared.education.mapper.EducationMapper;
import com.ofdun.jobfinder.shared.experience.mapper.JobExperienceMapper;
import com.ofdun.jobfinder.shared.language.mapper.LanguageMapper;
import com.ofdun.jobfinder.shared.skill.mapper.SkillMapper;

public class ResumeMapper {
    public static ResumeModel toModel(ResumeEntity resumeEntity) {
        if (resumeEntity == null) {
            return null;
        }
        return new ResumeModel(
                resumeEntity.getId(),
                resumeEntity.getApplicantId(),
                CategoryMapper.toModel(resumeEntity.getCategory()),
                resumeEntity.getDescription(),
                resumeEntity.getSkills() == null
                        ? null
                        : resumeEntity.getSkills().stream().map(SkillMapper::toModel).toList(),
                resumeEntity.getEducations() == null
                        ? null
                        : resumeEntity.getEducations().stream()
                                .map(EducationMapper::toModel)
                                .toList(),
                resumeEntity.getJobExperiences() == null
                        ? null
                        : resumeEntity.getJobExperiences().stream()
                                .map(JobExperienceMapper::toModel)
                                .toList(),
                resumeEntity.getLanguages() == null
                        ? null
                        : resumeEntity.getLanguages().stream()
                                .map(LanguageMapper::toModel)
                                .toList(),
                resumeEntity.getCreationDate());
    }

    public static ResumeEntity toEntity(ResumeModel resumeModel) {
        if (resumeModel == null) {
            return null;
        }
        return new ResumeEntity(
                resumeModel.getId(),
                resumeModel.getApplicantId(),
                CategoryMapper.toEntity(resumeModel.getCategory()),
                resumeModel.getDescription(),
                resumeModel.getSkills() == null
                        ? null
                        : resumeModel.getSkills().stream().map(SkillMapper::toEntity).toList(),
                resumeModel.getEducations() == null
                        ? null
                        : resumeModel.getEducations().stream()
                                .map(EducationMapper::toEntity)
                                .toList(),
                resumeModel.getJobExperiences() == null
                        ? null
                        : resumeModel.getJobExperiences().stream()
                                .map(JobExperienceMapper::toEntity)
                                .toList(),
                resumeModel.getLanguages() == null
                        ? null
                        : resumeModel.getLanguages().stream()
                                .map(LanguageMapper::toEntity)
                                .toList(),
                resumeModel.getDate());
    }
}
