package com.ofdun.jobfinder.features.auth.domain.model;

import lombok.Data;

@Data
public abstract class AccountModel {
    protected Long id;
    protected String email;
    protected String passwordHash;
}
