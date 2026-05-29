package com.ofdun.jobfinder.features.applicant.data.mongo.mapper;

import com.ofdun.jobfinder.features.applicant.data.mongo.entity.ApplicantDocument;
import com.ofdun.jobfinder.features.applicant.domain.model.ApplicantModel;

public class ApplicantMongoMapper {
    public static ApplicantDocument toEntity(ApplicantModel applicant) {
        if (applicant == null) {
            return null;
        }
        return new ApplicantDocument(
                applicant.getId(),
                applicant.getName(),
                applicant.getEmail(),
                applicant.getPasswordHash(),
                applicant.getAddress(),
                applicant.getPhoneNumber(),
                applicant.getLocationId());
    }

    public static ApplicantModel toModel(ApplicantDocument entity) {
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
                entity.getLocationId());
    }
}
