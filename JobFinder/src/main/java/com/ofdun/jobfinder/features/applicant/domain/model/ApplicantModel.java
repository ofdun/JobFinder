package com.ofdun.jobfinder.features.applicant.domain.model;

import com.ofdun.jobfinder.shared.location.model.LocationModel;
import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Data;

@AllArgsConstructor
@Data
public class ApplicantModel {

    private Long id;

    @NotBlank(message = "Name is required")
    @Size(min = 2, max = 100)
    @Pattern(regexp = ".*\\S.*", message = "Name cannot be only whitespace")
    private String name;

    @NotBlank(message = "Email is required")
    @Email(message = "Email should be valid")
    private String email;

    @NotBlank(message = "Password hash is required")
    private String passwordHash;

    private String address;

    @Pattern(regexp = "^\\+?[0-9. ()-]{7,25}$", message = "Phone number is invalid")
    private String phoneNumber;

    @NotNull(message = "Location is required")
    @Valid
    private LocationModel location;
}
