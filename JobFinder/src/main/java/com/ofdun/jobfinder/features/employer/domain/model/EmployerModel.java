package com.ofdun.jobfinder.features.employer.domain.model;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import org.hibernate.validator.constraints.URL;

@AllArgsConstructor
@Data
public class EmployerModel {
    private Long id;

    @NotBlank
    @Size(min = 2, max = 100)
    private String name;

    @NotBlank(message = "Password hash is required")
    private String passwordHash;

    private String description;

    private String address;

    @URL(message = "Site URL should be valid")
    private String websiteLink;

    @NotBlank
    @Email(message = "Email should be valid")
    private String email;

    @NotNull private Long locationId;
}
