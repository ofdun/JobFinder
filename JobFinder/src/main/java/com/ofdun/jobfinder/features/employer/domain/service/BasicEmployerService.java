package com.ofdun.jobfinder.features.employer.domain.service;

import com.ofdun.jobfinder.features.employer.domain.model.EmployerModel;
import com.ofdun.jobfinder.features.employer.domain.repository.EmployerRepository;
import com.ofdun.jobfinder.features.employer.domain.validator.EmployerValidator;
import com.ofdun.jobfinder.features.employer.exception.EmployerNotFoundException;

import jakarta.validation.Valid;

import lombok.NonNull;
import lombok.RequiredArgsConstructor;

import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class BasicEmployerService implements EmployerService {
    @org.springframework.beans.factory.annotation.Autowired
    private com.ofdun.jobfinder.features.auth.mfa.AuthenticationProtection protection;

    private final EmployerRepository employerRepository;
    private final EmployerValidator employerValidator;

    @Override
    public Long createEmployer(@NonNull @Valid EmployerModel employerModel) {
        employerValidator.validateEmployerForCreate(employerModel);
        return employerRepository.createEmployer(employerModel);
    }

    @Override
    public EmployerModel getEmployerById(@NonNull Long id) {
        return employerRepository
                .getEmployerById(id)
                .orElseThrow(() -> new EmployerNotFoundException(id));
    }

    @Override
    public EmployerModel updateEmployer(@NonNull @Valid EmployerModel employerModel) {
        if (protection != null)
            protection.checkProfilePasswordChange(
                    com.ofdun.jobfinder.features.auth.enums.AccountType.EMPLOYER,
                    employerModel.getId(),
                    employerModel.getPasswordHash());
        employerValidator.validateEmployerForUpdate(employerModel);
        return employerRepository.updateEmployer(employerModel);
    }

    @Override
    public Boolean deleteEmployer(@NonNull Long id) {
        employerValidator.validateEmployerForDelete(id);
        var deleted = employerRepository.deleteEmployer(id);
        if (Boolean.TRUE.equals(deleted) && protection != null)
            protection.remove(com.ofdun.jobfinder.features.auth.enums.AccountType.EMPLOYER, id);
        return deleted;
    }
}
