package com.ofdun.jobfinder.features.employer.domain.service;

import com.ofdun.jobfinder.features.employer.domain.model.EmployerModel;
import com.ofdun.jobfinder.features.employer.domain.repository.EmployerRepository;
import lombok.NonNull;
import org.springframework.stereotype.Service;

@Service
public class BasicEmployerService implements EmployerService {
    EmployerRepository employerRepository;

    @Override
    @NonNull
    public Long createEmployer(EmployerModel employerModel) {
        return employerRepository.createEmployer(employerModel);
    }

    @Override
    @NonNull
    public EmployerModel getEmployerById(Long id) {
        return employerRepository.getEmployerById(id);
    }

    @Override
    @NonNull
    public EmployerModel updateEmployer(EmployerModel employerModel) {
        return employerRepository.updateEmployer(employerModel);
    }

    @Override
    @NonNull
    public Boolean deleteEmployer(Long id) {
        return employerRepository.deleteEmployer(id);
    }
}
