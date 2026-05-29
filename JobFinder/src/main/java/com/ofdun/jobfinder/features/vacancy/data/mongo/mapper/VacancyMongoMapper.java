package com.ofdun.jobfinder.features.vacancy.data.mongo.mapper;

import com.ofdun.jobfinder.features.vacancy.data.mongo.entity.VacancyDocument;
import com.ofdun.jobfinder.features.vacancy.domain.model.VacancyModel;

public class VacancyMongoMapper {
    public static VacancyDocument toEntity(VacancyModel model) {
        if (model == null) {
            return null;
        }
        return new VacancyDocument(
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
                model.getAddress(),
                model.getStatus());
    }

    public static VacancyModel toModel(VacancyDocument entity) {
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
                entity.getAddress(),
                entity.getStatus());
    }
}

