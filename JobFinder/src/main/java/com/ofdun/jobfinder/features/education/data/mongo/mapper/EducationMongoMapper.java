package com.ofdun.jobfinder.features.education.data.mongo.mapper;

import com.ofdun.jobfinder.features.education.data.mongo.entity.EducationDocument;
import com.ofdun.jobfinder.features.education.domain.model.EducationModel;
import java.time.Year;

public class EducationMongoMapper {
    public static EducationModel toModel(EducationDocument entity) {
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

    public static EducationDocument toEntity(EducationModel model) {
        if (model == null) {
            return null;
        }
        return new EducationDocument(
                model.getId(),
                model.getResumeId(),
                model.getEducationDegree(),
                model.getInstitutionName(),
                model.getFaculty(),
                model.getDepartment(),
                model.getYearOfGraduation());
    }
}

