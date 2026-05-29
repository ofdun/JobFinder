package com.ofdun.jobfinder.features.auth.data.repository;

import com.ofdun.jobfinder.features.auth.data.mapper.EmployerAccountMapper;
import com.ofdun.jobfinder.features.auth.domain.model.EmployerAccountModel;
import com.ofdun.jobfinder.features.auth.domain.repository.EmployerAccountRepository;
import com.ofdun.jobfinder.features.employer.domain.repository.EmployerRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@ConditionalOnProperty(name = "app.persistence.type", havingValue = "mongo")
public class MongoEmployerAccountRepository implements EmployerAccountRepository {
    private final EmployerRepository employerRepository;

    @Override
    public EmployerAccountModel findByEmail(String email) {
        return employerRepository.getEmployerByEmail(email).map(EmployerAccountMapper::toModel).orElse(null);
    }
}

