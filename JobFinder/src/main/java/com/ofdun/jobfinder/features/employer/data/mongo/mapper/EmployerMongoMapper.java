package com.ofdun.jobfinder.features.employer.data.mongo.mapper;

import com.ofdun.jobfinder.features.employer.data.mongo.entity.EmployerDocument;
import com.ofdun.jobfinder.features.employer.domain.model.EmployerModel;

public class EmployerMongoMapper {
    public static EmployerDocument toEntity(EmployerModel model) {
        if (model == null) {
            return null;
        }
        return new EmployerDocument(
                model.getId(),
                model.getName(),
                model.getPasswordHash(),
                model.getDescription(),
                model.getAddress(),
                model.getWebsiteLink(),
                model.getEmail(),
                model.getLocationId());
    }

    public static EmployerModel toModel(EmployerDocument entity) {
        if (entity == null) {
            return null;
        }
        return new EmployerModel(
                entity.getId(),
                entity.getName(),
                entity.getPasswordHash(),
                entity.getDescription(),
                entity.getAddress(),
                entity.getWebsiteLink(),
                entity.getEmail(),
                entity.getLocationId());
    }
}

