package com.ofdun.jobfinder.features.auth.domain.repository;

import com.ofdun.jobfinder.features.auth.domain.model.ApplicantAccountModel;

import java.util.Optional;

public interface ApplicantAccountRepository {
    Optional<ApplicantAccountModel> findByEmail(String email);
}
