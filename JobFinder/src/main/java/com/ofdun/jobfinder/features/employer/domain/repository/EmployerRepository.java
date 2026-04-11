package com.ofdun.jobfinder.features.employer.domain.repository;

import com.ofdun.jobfinder.features.employer.domain.model.EmployerModel;
import java.util.Optional;

public interface EmployerRepository {
    Long createEmployer(EmployerModel employerModel);

    Optional<EmployerModel> getEmployerById(Long id);

    Optional<EmployerModel> getEmployerByEmail(String email);

    EmployerModel updateEmployer(EmployerModel employerModel);

    Boolean deleteEmployer(Long id);
}
