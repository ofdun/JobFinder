package com.ofdun.jobfinder.features.auth.domain.model;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public abstract class AccountModel {
    protected Long id;

    @NotBlank(message = "Email is required")
    @Email(message = "Email should be valid")
    protected String email;

    @NotBlank(message = "Password hash is required")
    protected String passwordHash;
}
