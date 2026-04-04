package com.ofdun.jobfinder.features.employer.domain.validator;

import com.ofdun.jobfinder.features.employer.domain.model.EmployerModel;

public interface EmployerValidator {
    void validateEmployerForCreate(EmployerModel model);
    void validateEmployerForUpdate(EmployerModel model);
    void validateEmployerForDelete(Long id);
}
