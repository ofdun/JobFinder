package com.ofdun.jobfinder.features.experience.data.mongo.entity;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDate;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@Data
@Document(collection = "experiences")
@AllArgsConstructor
@NoArgsConstructor
public class JobExperienceDocument {
    @Id
    private Long id;

    @NotNull private Long resumeId;

    @NotBlank private String position;

    @NotBlank private String companyName;

    private String description;

    @NotNull private LocalDate startDate;

    @NotNull private LocalDate endDate;
}

