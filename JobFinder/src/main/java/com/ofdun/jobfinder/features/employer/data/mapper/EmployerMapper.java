package com.ofdun.jobfinder.features.employer.data.mapper;

import com.ofdun.jobfinder.features.employer.data.entity.EmployerEntity;
import com.ofdun.jobfinder.features.employer.domain.model.EmployerModel;
import com.ofdun.jobfinder.shared.location.mapper.LocationMapper;

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
                model.getSiteUrl(),
                model.getEmail(),
                LocationMapper.toEntity(model.getLocation()));
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
                LocationMapper.toModel(entity.getLocation()));
    }
}
