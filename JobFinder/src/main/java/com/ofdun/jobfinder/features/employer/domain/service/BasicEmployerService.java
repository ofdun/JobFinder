package com.ofdun.jobfinder.features.employer.domain.service;

import com.ofdun.jobfinder.features.auth.enums.AccountType;
import com.ofdun.jobfinder.features.auth.mfa.AccountSecurity;
import com.ofdun.jobfinder.features.employer.domain.model.EmployerModel;
import com.ofdun.jobfinder.features.employer.domain.repository.EmployerRepository;
import com.ofdun.jobfinder.features.employer.domain.validator.EmployerValidator;
import com.ofdun.jobfinder.features.employer.exception.EmployerNotFoundException;
import io.micrometer.observation.annotation.Observed;
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
    @Observed(
            name = "jobfinder.employer.service.createEmployer",
            contextualName = "Создание работодателя",
            lowCardinalityKeyValues = {"feature", "employer", "layer", "service"})
    public Long createEmployer(@NonNull @Valid EmployerModel employerModel) {
        employerValidator.validateEmployerForCreate(employerModel);
        return employerRepository.createEmployer(employerModel);
    }

    @Override
    @Observed(
            name = "jobfinder.employer.service.getEmployerById",
            contextualName = "Получение работодателя по ID",
            lowCardinalityKeyValues = {"feature", "employer", "layer", "service"})
    public EmployerModel getEmployerById(@NonNull Long id) {
        return employerRepository
                .getEmployerById(id)
                .orElseThrow(() -> new EmployerNotFoundException(id));
    }

    @Override
    @Observed(
            name = "jobfinder.employer.service.updateEmployer",
            contextualName = "Обновление работодателя",
            lowCardinalityKeyValues = {"feature", "employer", "layer", "service"})
    public EmployerModel updateEmployer(@NonNull @Valid EmployerModel employerModel) {
        protection.checkProfilePasswordChange(
                AccountType.EMPLOYER, employerModel.getId(), employerModel.getPasswordHash());
        employerValidator.validateEmployerForUpdate(employerModel);
        return employerRepository.updateEmployer(employerModel);
    }

    @Override
    @Observed(
            name = "jobfinder.employer.service.deleteEmployer",
            contextualName = "Удаление работодателя",
            lowCardinalityKeyValues = {"feature", "employer", "layer", "service"})
    public Boolean deleteEmployer(@NonNull Long id) {
        employerValidator.validateEmployerForDelete(id);
        var deleted = employerRepository.deleteEmployer(id);
        if (Boolean.TRUE.equals(deleted)) protection.remove(AccountType.EMPLOYER, id);
        return deleted;
    }
}
