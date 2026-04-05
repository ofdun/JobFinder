package com.ofdun.jobfinder.shared.experience.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDate;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Entity
@Table(schema = "jobfinder", name = "experiences")
@AllArgsConstructor
@NoArgsConstructor
public class JobExperienceEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotNull private Long resumeId;

    @NotBlank private String position;

    @NotBlank private String companyName;

    private String description;

    @NotNull private LocalDate startDate;

    @NotNull private LocalDate endDate;
}
