package com.ofdun.jobfinder.features.vacancy.api.mapper;

import com.ofdun.jobfinder.features.language.api.mapper.LanguageApiMapper;
import com.ofdun.jobfinder.features.language.domain.model.LanguageModel;
import com.ofdun.jobfinder.features.location.api.mapper.LocationApiMapper;
import com.ofdun.jobfinder.features.location.domain.model.LocationModel;
import com.ofdun.jobfinder.features.skill.api.mapper.SkillApiMapper;
import com.ofdun.jobfinder.features.skill.domain.model.SkillModel;
import com.ofdun.jobfinder.features.vacancy.api.dto.VacancyRequest;
import com.ofdun.jobfinder.features.vacancy.api.dto.VacancyResponse;
import com.ofdun.jobfinder.features.vacancy.domain.model.VacancyModel;

import java.util.Date;
import java.util.List;

import org.springframework.stereotype.Component;

@Component
public class VacancyApiMapper {
    public VacancyModel toModel(VacancyRequest request) {
        if (request == null) return null;
        return new VacancyModel(
                null,
                request.getEmployerId(),
                request.getLocationId(),
                request.getSalary(),
                request.getSkillIds(),
                request.getLanguageIds(),
                request.getPaymentFrequency(),
                request.getExperience(),
                request.getJobFormat(),
                request.getEmploymentType(),
                request.getDescription(),
                new Date(),
                request.getAddress());
    }

    public VacancyResponse toResponse(VacancyModel model, LocationModel location,
                                      List<SkillModel> skills, List<LanguageModel> languages) {
        if (model == null) return null;
        return new VacancyResponse(
                model.getId(),
                model.getEmployerId(),
                LocationApiMapper.toDto(location),
                model.getSalary(),
                skills == null ? null : skills.stream().map(SkillApiMapper::toDto).toList(),
                languages == null ? null : languages.stream().map(LanguageApiMapper::toDto).toList(),
                model.getPaymentFrequency(),
                model.getExperience(),
                model.getJobFormat(),
                model.getEmploymentType(),
                model.getDescription(),
                model.getPublicationDate(),
                model.getAddress());
    }
}

