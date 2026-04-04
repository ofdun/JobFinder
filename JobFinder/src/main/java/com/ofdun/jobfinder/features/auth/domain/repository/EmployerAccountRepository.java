package com.ofdun.jobfinder.features.auth.domain.repository;

import com.ofdun.jobfinder.features.auth.domain.model.EmployerAccountModel;

public interface EmployerAccountRepository {
    EmployerAccountModel findByEmail(String email);
}
