package com.ofdun.jobfinder.features.vacancy.data.mapper;

import com.ofdun.jobfinder.features.vacancy.data.entity.VacancyEntity;
import com.ofdun.jobfinder.features.vacancy.domain.model.VacancyModel;
import com.ofdun.jobfinder.shared.language.mapper.LanguageMapper;
import com.ofdun.jobfinder.shared.location.mapper.LocationMapper;
import com.ofdun.jobfinder.shared.skill.mapper.SkillMapper;

public class VacancyMapper {
    public static VacancyEntity toEntity(VacancyModel model) {
        if (model == null) {
            return null;
        }
        return new VacancyEntity(
                model.getId(),
                model.getEmployerId(),
                LocationMapper.toEntity(model.getLocation()),
                model.getSalary(),
                model.getSkills() == null
                        ? null
                        : model.getSkills().stream().map(SkillMapper::toEntity).toList(),
                model.getLanguages() == null
                        ? null
                        : model.getLanguages().stream().map(LanguageMapper::toEntity).toList(),
                model.getPaymentFrequency(),
                model.getExperience(),
                model.getJobFormat(),
                model.getEmploymentType(),
                model.getDescription(),
                model.getPublicationDate(),
                model.getAddress());
    }

    public static VacancyModel toModel(VacancyEntity entity) {
        if (entity == null) {
            return null;
        }
        return new VacancyModel(
                entity.getId(),
                entity.getEmployerId(),
                LocationMapper.toModel(entity.getLocation()),
                entity.getSalary(),
                entity.getSkills() == null
                        ? null
                        : entity.getSkills().stream().map(SkillMapper::toModel).toList(),
                entity.getLanguages() == null
                        ? null
                        : entity.getLanguages().stream().map(LanguageMapper::toModel).toList(),
                entity.getPaymentFrequency(),
                entity.getWorkExperience(),
                entity.getWorkFormat(),
                entity.getEmploymentType(),
                entity.getDescription(),
                entity.getPublicationDate(),
                entity.getAddress());
    }
}
