package com.ofdun.jobfinder.features.auth.data.mapper;

import com.ofdun.jobfinder.features.applicant.domain.model.ApplicantModel;
import com.ofdun.jobfinder.features.auth.domain.model.ApplicantAccountModel;

public class ApplicantAccountMapper {
    public static ApplicantAccountModel toModel(ApplicantModel model) {
        if (model == null) {
            return null;
        }
        return new ApplicantAccountModel(model.getId(), model.getEmail(), model.getPasswordHash());
    }
}
