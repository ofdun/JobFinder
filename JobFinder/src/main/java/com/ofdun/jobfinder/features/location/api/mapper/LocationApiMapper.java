package com.ofdun.jobfinder.features.location.api.mapper;

import com.ofdun.jobfinder.features.location.api.dto.LocationDto;
import com.ofdun.jobfinder.features.location.domain.model.LocationModel;

public class LocationApiMapper {
    public static LocationModel toModel(LocationDto dto) {
        if (dto == null) return null;
        return new LocationModel(dto.getId(), dto.getCity(), dto.getCountry());
    }

    public static LocationDto toDto(LocationModel model) {
        if (model == null) return null;
        return new LocationDto(model.getId(), model.getCity(), model.getCountry());
    }
}
