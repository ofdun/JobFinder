package com.ofdun.jobfinder.features.employer.data.mongo.entity;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.validator.constraints.URL;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@Data
@Document(collection = "employers")
@AllArgsConstructor
@NoArgsConstructor
public class EmployerDocument {
    @Id
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

