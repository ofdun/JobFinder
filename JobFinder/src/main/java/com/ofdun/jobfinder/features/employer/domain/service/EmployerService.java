package com.ofdun.jobfinder.features.employer.domain.service;

import com.ofdun.jobfinder.features.employer.domain.model.EmployerModel;

public interface EmployerService {
    Long createEmployer(EmployerModel employerModel);
    EmployerModel getEmployerById(Long id);
    EmployerModel updateEmployer(EmployerModel employerModel);
    Boolean deleteEmployer(Long id);
}
