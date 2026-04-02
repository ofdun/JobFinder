package com.ofdun.jobfinder.features.applicant.data.mapper;

import com.ofdun.jobfinder.features.applicant.data.entity.ApplicantEntity;
import com.ofdun.jobfinder.features.applicant.domain.model.ApplicantModel;
import com.ofdun.jobfinder.shared.data.entity.LocationEntity;
import com.ofdun.jobfinder.shared.domain.model.LocationModel;

public class ApplicantMapper {
    public static ApplicantEntity toEntity(ApplicantModel applicant) {
        return new ApplicantEntity(
                applicant.getId(),
                applicant.getName(),
                applicant.getEmail(),
                applicant.getPasswordHash(),
                applicant.getAddress(),
                applicant.getPhoneNumber(),
                new LocationEntity(null, applicant.getLocation().getCity(), applicant.getLocation().getCountry())
        );
    }

    public static ApplicantModel toModel(ApplicantEntity entity) {
        return new ApplicantModel(
                entity.getId(),
                entity.getName(),
                entity.getEmail(),
                entity.getPasswordHash(),
                entity.getAddress(),
                entity.getPhoneNumber(),
                new LocationModel(entity.getLocation().getId(), entity.getLocation().getCity(), entity.getLocation().getCountry())
        );
    }
}
