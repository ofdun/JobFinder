package com.ofdun.jobfinder.features.auth.data.repository;

import com.ofdun.jobfinder.features.auth.domain.model.ApplicantAccountModel;
import com.ofdun.jobfinder.features.auth.domain.model.EmployerAccountModel;
import com.ofdun.jobfinder.features.auth.domain.repository.ApplicantAccountRepository;
import com.ofdun.jobfinder.features.auth.domain.repository.EmployerAccountRepository;
import org.springframework.stereotype.Component;

@Component
public class PosgreSQLApplicantAccountRepository implements ApplicantAccountRepository {
    @Override
    public ApplicantAccountModel findByEmail(String email) {
        return null;
    }
}
