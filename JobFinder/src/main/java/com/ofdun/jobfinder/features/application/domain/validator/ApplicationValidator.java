package com.ofdun.jobfinder.features.application.domain.validator;


import com.ofdun.jobfinder.features.application.domain.model.ApplicationModel;

public interface ApplicationValidator {
    void validateApplicationForCreate(ApplicationModel model);
    void validateApplicationForUpdate(ApplicationModel model);
    void validateApplicationForDelete(Long applicationId);
}
