package com.ofdun.jobfinder.features.vacancy.data.mapper;

import com.ofdun.jobfinder.features.vacancy.data.entity.VacancyEntity;
import com.ofdun.jobfinder.features.vacancy.domain.model.VacancyModel;

public class VacancyMapper {
    public static VacancyEntity toEntity(VacancyModel model) {
        if (model == null) {
            return null;
        }
        return new VacancyEntity(
                model.getId(),
                model.getEmployerId(),
                model.getLocationId(),
                model.getSalary(),
                model.getSkillIds(),
                model.getLanguageIds(),
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
                entity.getLocationId(),
                entity.getSalary(),
                entity.getSkillIds(),
                entity.getLanguageIds(),
                entity.getPaymentFrequency(),
                entity.getWorkExperience(),
                entity.getWorkFormat(),
                entity.getEmploymentType(),
                entity.getDescription(),
                entity.getPublicationDate(),
                entity.getAddress());
    }
}
