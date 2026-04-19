package com.ofdun.jobfinder.features.education.api.mapper;

import com.ofdun.jobfinder.features.education.api.dto.EducationCreateDto;
import com.ofdun.jobfinder.features.education.api.dto.EducationDto;
import com.ofdun.jobfinder.features.education.domain.model.EducationModel;

public class EducationApiMapper {
    public static EducationDto toEducationDto(EducationModel model) {
        if (model == null) return null;
        return new EducationDto(
                model.getId(),
                model.getResumeId(),
                model.getEducationDegree(),
                model.getInstitutionName(),
                model.getFaculty(),
                model.getDepartment(),
                model.getYearOfGraduation());
    }

    public static EducationModel toEducationModel(EducationCreateDto dto) {
        if (dto == null) return null;
        return new EducationModel(
                null,
                null,
                dto.getEducationDegree(),
                dto.getInstitutionName(),
                dto.getFaculty(),
                dto.getDepartment(),
                dto.getYearOfGraduation());
    }
}
