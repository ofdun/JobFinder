package com.ofdun.jobfinder.features.employer.data.mapper;

import com.ofdun.jobfinder.features.employer.data.entity.EmployerEntity;
import com.ofdun.jobfinder.features.employer.domain.model.EmployerModel;

public class EmployerMapper {
    public static EmployerEntity toEntity(EmployerModel model) {
        if (model == null) {
            return null;
        }
        return new EmployerEntity(
                model.getId(),
                model.getName(),
                model.getPasswordHash(),
                model.getDescription(),
                model.getAddress(),
                model.getWebsiteLink(),
                model.getEmail(),
                model.getLocationId());
    }

    public static EmployerModel toModel(EmployerEntity entity) {
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
