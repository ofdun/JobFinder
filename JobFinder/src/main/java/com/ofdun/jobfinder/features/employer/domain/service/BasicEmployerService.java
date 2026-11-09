package com.ofdun.jobfinder.features.employer.domain.service;

import com.ofdun.jobfinder.features.auth.enums.AccountType;
import com.ofdun.jobfinder.features.auth.mfa.AccountSecurity;
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
    private final AccountSecurity protection;

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
        protection.checkProfilePasswordChange(
                AccountType.EMPLOYER, employerModel.getId(), employerModel.getPasswordHash());
        employerValidator.validateEmployerForUpdate(employerModel);
        return employerRepository.updateEmployer(employerModel);
    }

    @Override
    public Boolean deleteEmployer(@NonNull Long id) {
        employerValidator.validateEmployerForDelete(id);
        var deleted = employerRepository.deleteEmployer(id);
        if (Boolean.TRUE.equals(deleted)) protection.remove(AccountType.EMPLOYER, id);
        return deleted;
    }
}
