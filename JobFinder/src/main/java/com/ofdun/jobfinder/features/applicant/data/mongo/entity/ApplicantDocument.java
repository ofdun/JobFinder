package com.ofdun.jobfinder.features.applicant.data.mongo.entity;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@Data
@Document(collection = "applicants")
@AllArgsConstructor
@NoArgsConstructor
public class ApplicantDocument {
    @Id
    private Long id;

    @NotNull
    @Size(min = 1, max = 50)
    @Pattern(regexp = ".*\\S.*", message = "Name cannot be only whitespace")
    private String name;

    @NotNull
    @Email
    @Size(min = 1, max = 50)
    private String email;

    @NotNull
    @Size(min = 1, max = 255)
    private String passwordHash;

    private String address;

    @NotNull private String phoneNumber;

    @NotNull private Long locationId;
}
