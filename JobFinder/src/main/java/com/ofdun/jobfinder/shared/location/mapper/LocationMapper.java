package com.ofdun.jobfinder.shared.location.mapper;

import com.ofdun.jobfinder.shared.location.entity.LocationEntity;
import com.ofdun.jobfinder.shared.location.model.LocationModel;

public class LocationMapper {
    public static LocationEntity toEntity(LocationModel model) {
        if (model == null) {
            return null;
        }
        return new LocationEntity(model.getId(), model.getCity(), model.getCountry());
    }

    public static LocationModel toModel(LocationEntity entity) {
        if (entity == null) {
            return null;
        }
        return new LocationModel(entity.getId(), entity.getCity(), entity.getCountry());
    }
}
