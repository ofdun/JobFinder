package com.ofdun.jobfinder.features.employer.domain.validator;

import com.ofdun.jobfinder.features.employer.domain.model.EmployerModel;
import com.ofdun.jobfinder.features.employer.domain.repository.EmployerRepository;
import com.ofdun.jobfinder.features.employer.exception.EmployerAlreadyExistsException;
import com.ofdun.jobfinder.features.employer.exception.EmployerNotFoundException;
import io.micrometer.observation.annotation.Observed;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class BasicEmployerValidator implements EmployerValidator {
    private final EmployerRepository employerRepository;

    @Override
    @Observed(
            name = "jobfinder.employer.validation.validateEmployerForCreate",
            contextualName = "Проверка работодателя перед созданием",
            lowCardinalityKeyValues = {"feature", "employer", "layer", "validation"})
    public void validateEmployerForCreate(EmployerModel model) {
        validateAlreadyExists(model.getEmail());
    }

    @Override
    @Observed(
            name = "jobfinder.employer.validation.validateEmployerForUpdate",
            contextualName = "Проверка работодателя перед обновлением",
            lowCardinalityKeyValues = {"feature", "employer", "layer", "validation"})
    public void validateEmployerForUpdate(EmployerModel model) {
        validateId(model.getId());
        validateNotExists(model.getId());
    }

    @Override
    @Observed(
            name = "jobfinder.employer.validation.validateEmployerForDelete",
            contextualName = "Проверка работодателя перед удалением",
            lowCardinalityKeyValues = {"feature", "employer", "layer", "validation"})
    public void validateEmployerForDelete(Long id) {
        validateId(id);
        validateNotExists(id);
    }

    private void validateNotExists(Long id) {
        if (employerRepository.getEmployerById(id).isEmpty()) {
            throw new EmployerNotFoundException(id);
        }
    }

    private void validateAlreadyExists(String email) {
        if (employerRepository.getEmployerByEmail(email).isPresent()) {
            throw new EmployerAlreadyExistsException(email);
        }
    }

    private void validateId(Long id) {
        if (id == null || id <= 0) {
            throw new IllegalArgumentException("Invalid employer ID");
        }
    }
}
