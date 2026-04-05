package com.ofdun.jobfinder.features.auth.data.mapper;

import com.ofdun.jobfinder.features.auth.domain.model.EmployerAccountModel;
import com.ofdun.jobfinder.features.employer.domain.model.EmployerModel;

public class EmployerAccountMapper {
    public static EmployerAccountModel toModel(EmployerModel model) {
        if (model == null) {
            return null;
        }
        return new EmployerAccountModel(model.getId(), model.getEmail(), model.getPasswordHash());
    }
}
