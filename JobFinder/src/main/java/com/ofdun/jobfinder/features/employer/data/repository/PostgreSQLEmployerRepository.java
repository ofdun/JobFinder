package com.ofdun.jobfinder.features.employer.data.repository;

import com.ofdun.jobfinder.features.employer.domain.model.EmployerModel;
import com.ofdun.jobfinder.features.employer.domain.repository.EmployerRepository;
import org.springframework.stereotype.Component;

@Component
public class PostgreSQLEmployerRepository implements EmployerRepository {
    @Override
    public Long createEmployer(EmployerModel employerModel) {
        return 0L;
    }

    @Override
    public EmployerModel getEmployerById(Long id) {
        return null;
    }

    @Override
    public EmployerModel getEmployerByEmail(String email) {
        return null;
    }

    @Override
    public EmployerModel updateEmployer(EmployerModel employerModel) {
        return employerModel;
    }

    @Override
    public Boolean deleteEmployer(Long id) {
        return true;
    }
}
