package com.ofdun.jobfinder.features.employer.data.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.validator.constraints.URL;

@Data
@Entity
@Table(schema = "jobfinder", name = "employers")
@AllArgsConstructor
@NoArgsConstructor
public class EmployerEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
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

    @NotNull
    private Long locationId;
}
