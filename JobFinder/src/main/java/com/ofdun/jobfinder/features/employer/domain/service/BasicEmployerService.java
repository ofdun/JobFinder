package com.ofdun.jobfinder.features.employer.domain.service;

import com.ofdun.jobfinder.features.employer.domain.model.EmployerModel;
import com.ofdun.jobfinder.features.employer.domain.repository.EmployerRepository;
import lombok.NonNull;
import org.springframework.stereotype.Service;

@Service
public class BasicEmployerService implements EmployerService {
    private final EmployerRepository employerRepository;

    public BasicEmployerService(@NonNull EmployerRepository employerRepository) {
        this.employerRepository = employerRepository;
    }

    @Override
    public Long createEmployer(@NonNull EmployerModel employerModel) {
        if (employerRepository.getEmployerByEmail(employerModel.getEmail()) != null) {
            throw new IllegalArgumentException("Employer with the same email already exists");
        }
        return employerRepository.createEmployer(employerModel);
    }

    @Override
    public EmployerModel getEmployerById(@NonNull Long id) {
        return employerRepository.getEmployerById(id);
    }

    @Override
    public EmployerModel updateEmployer(@NonNull EmployerModel employerModel) {
        if (employerRepository.getEmployerById(employerModel.getId()) == null) {
            throw new IllegalArgumentException("Employer not found");
        }
        return employerRepository.updateEmployer(employerModel);
    }

    @Override
    public Boolean deleteEmployer(@NonNull Long id) {
        if (!employerRepository.deleteEmployer(id)) {
            throw new IllegalArgumentException("Employer not found");
        }
        return true;
    }
}

