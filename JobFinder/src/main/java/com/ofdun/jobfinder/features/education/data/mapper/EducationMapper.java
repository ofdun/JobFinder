package com.ofdun.jobfinder.features.education.data.mapper;

import com.ofdun.jobfinder.features.education.data.entity.EducationEntity;
import com.ofdun.jobfinder.features.education.domain.model.EducationModel;

public class EducationMapper {
    public static EducationModel toModel(EducationEntity entity) {
        if (entity == null) {
            return null;
        }
        return new EducationModel(
                entity.getId(),
                entity.getResumeId(),
                entity.getDegree(),
                entity.getInstitution(),
                entity.getFaculty(),
                entity.getDepartment(),
                entity.getGraduationYear());
    }

    public static EducationEntity toEntity(EducationModel model) {
        if (model == null) {
            return null;
        }
        return new EducationEntity(
                model.getId(),
                model.getResumeId(),
                model.getEducationDegree(),
                model.getInstitutionName(),
                model.getFaculty(),
                model.getDepartment(),
                model.getYearOfGraduation());
    }
}
