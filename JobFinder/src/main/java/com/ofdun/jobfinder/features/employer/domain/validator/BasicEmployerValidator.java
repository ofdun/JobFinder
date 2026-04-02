package com.ofdun.jobfinder.features.employer.domain.validator;

import com.ofdun.jobfinder.features.employer.exception.EmployerAlreadyExistsException;
import com.ofdun.jobfinder.features.employer.exception.EmployerNotFoundException;
import com.ofdun.jobfinder.features.employer.domain.model.EmployerModel;
import com.ofdun.jobfinder.features.employer.domain.repository.EmployerRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class BasicEmployerValidator implements EmployerValidator {
    private final EmployerRepository employerRepository;

    @Override
    public void validateEmployerForCreate(EmployerModel model) {
        validateAlreadyExists(model.getEmail());
    }

    @Override
    public void validateEmployerForUpdate(EmployerModel model) {
        validateId(model.getId());
        validateNotExists(model.getId());
    }

    @Override
    public void validateEmployerForDelete(Long id) {
        validateId(id);
        validateNotExists(id);
    }

    private void validateNotExists(Long id) {
        if (employerRepository.getEmployerById(id) == null) {
            throw new EmployerNotFoundException(id);
        }
    }

    private void validateAlreadyExists(String email) {
        if (employerRepository.getEmployerByEmail(email) != null) {
            throw new EmployerAlreadyExistsException(email);
        }
    }

    private void validateId(Long id) {
        if (id == null || id <= 0) {
            throw new IllegalArgumentException("Invalid employer ID");
        }
    }
}
