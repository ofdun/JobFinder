package com.ofdun.jobfinder.features.auth.data.repository;

import com.ofdun.jobfinder.features.auth.domain.model.EmployerAccountModel;
import com.ofdun.jobfinder.features.auth.domain.repository.EmployerAccountRepository;
import org.springframework.stereotype.Component;

@Component
public class PosgreSQLEmployerAccountRepository implements EmployerAccountRepository {
    @Override
    public EmployerAccountModel findByEmail(String email) {
        return null;
    }
}
