package com.ofdun.jobfinder.features.employer.domain.repository;

import com.ofdun.jobfinder.features.employer.domain.model.EmployerModel;

public interface EmployerRepository {
    Long createEmployer(EmployerModel employerModel);
    EmployerModel getEmployerById(Long id);
    EmployerModel updateEmployer(EmployerModel employerModel);
    Boolean deleteEmployer(Long id);
}
