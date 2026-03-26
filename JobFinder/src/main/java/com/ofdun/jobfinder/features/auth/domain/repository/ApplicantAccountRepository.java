package com.ofdun.jobfinder.features.auth.domain.repository;

import com.ofdun.jobfinder.features.auth.domain.model.ApplicantAccountModel;

public interface ApplicantAccountRepository {
    ApplicantAccountModel findByEmail(String email);
}
