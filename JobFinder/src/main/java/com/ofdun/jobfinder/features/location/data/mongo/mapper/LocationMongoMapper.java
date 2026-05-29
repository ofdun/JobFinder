package com.ofdun.jobfinder.features.location.data.mongo.mapper;

import com.ofdun.jobfinder.features.location.data.mongo.entity.LocationDocument;
import com.ofdun.jobfinder.features.location.domain.model.LocationModel;

public class LocationMongoMapper {
    public static LocationDocument toEntity(LocationModel model) {
        if (model == null) {
            return null;
        }
        return new LocationDocument(model.getId(), model.getCity(), model.getCountry());
    }

    public static LocationModel toModel(LocationDocument entity) {
        if (entity == null) {
            return null;
        }
        return new LocationModel(entity.getId(), entity.getCity(), entity.getCountry());
    }
}

