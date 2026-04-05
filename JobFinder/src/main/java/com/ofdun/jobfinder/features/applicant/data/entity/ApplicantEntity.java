package com.ofdun.jobfinder.features.applicant.data.entity;

import com.ofdun.jobfinder.shared.location.entity.LocationEntity;
import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Entity
@Table(schema = "jobfinder", name = "applicants")
@AllArgsConstructor
@NoArgsConstructor
public class ApplicantEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
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

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "location_id", nullable = false)
    @NotNull
    private LocationEntity location;
}
