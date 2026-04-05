package com.ofdun.jobfinder.features.applicant.data.mapper;

import com.ofdun.jobfinder.features.applicant.data.entity.ApplicantEntity;
import com.ofdun.jobfinder.features.applicant.domain.model.ApplicantModel;
import com.ofdun.jobfinder.shared.location.mapper.LocationMapper;

public class ApplicantMapper {
    public static ApplicantEntity toEntity(ApplicantModel applicant) {
        if (applicant == null) {
            return null;
        }
        return new ApplicantEntity(
                applicant.getId(),
                applicant.getName(),
                applicant.getEmail(),
                applicant.getPasswordHash(),
                applicant.getAddress(),
                applicant.getPhoneNumber(),
                LocationMapper.toEntity(applicant.getLocation()));
    }

    public static ApplicantModel toModel(ApplicantEntity entity) {
        if (entity == null) {
            return null;
        }
        return new ApplicantModel(
                entity.getId(),
                entity.getName(),
                entity.getEmail(),
                entity.getPasswordHash(),
                entity.getAddress(),
                entity.getPhoneNumber(),
                LocationMapper.toModel(entity.getLocation()));
    }
}
