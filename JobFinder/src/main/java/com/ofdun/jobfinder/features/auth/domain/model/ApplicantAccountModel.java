package com.ofdun.jobfinder.features.auth.domain.model;

import lombok.EqualsAndHashCode;

@EqualsAndHashCode(callSuper = true)
public class ApplicantAccountModel extends AccountModel {
    public ApplicantAccountModel(Long id, String email, String passwordHash) {
        super(id, email, passwordHash);
    }
}
