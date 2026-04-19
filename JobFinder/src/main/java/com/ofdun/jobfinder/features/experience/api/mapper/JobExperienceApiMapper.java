package com.ofdun.jobfinder.features.experience.api.mapper;

import com.ofdun.jobfinder.features.experience.api.dto.JobExperienceCreateDto;
import com.ofdun.jobfinder.features.experience.api.dto.JobExperienceDto;
import com.ofdun.jobfinder.features.experience.domain.model.JobExperienceModel;

public class JobExperienceApiMapper {
    public static JobExperienceModel toJobExperienceModel(JobExperienceCreateDto dto) {
        if (dto == null) return null;
        return new JobExperienceModel(
                null,
                null,
                dto.getPosition(),
                dto.getCompanyName(),
                dto.getDescription(),
                dto.getStartDate(),
                dto.getEndDate());
    }

    public static JobExperienceDto toJobExperienceDto(JobExperienceModel model) {
        if (model == null) return null;
        return new JobExperienceDto(
                model.getId(),
                model.getResumeId(),
                model.getPosition(),
                model.getCompanyName(),
                model.getDescription(),
                model.getStartDate(),
                model.getEndDate());
    }
}
