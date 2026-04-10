package com.ofdun.jobfinder.features.auth.domain.model;

import lombok.EqualsAndHashCode;

@EqualsAndHashCode(callSuper = true)
public class EmployerAccountModel extends AccountModel {
    public EmployerAccountModel(Long id, String email, String passwordHash) {
        super(id, email, passwordHash);
    }
}
