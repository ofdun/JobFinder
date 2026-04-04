package com.ofdun.jobfinder.features.application.data.entity;

import com.ofdun.jobfinder.shared.application.domain.enums.ApplicationStatus;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;

@Data
@Entity(name = "applications")
@AllArgsConstructor
@NoArgsConstructor
public class ApplicationEntity {
    @Id
    @NotNull
    private Long id;

    @NotNull
    private Long vacancyId;

    @NotNull
    private Long resumeId;

    @NotNull
    private Date applicationDate;

    @NotNull
    private ApplicationStatus applicationStatus;
}
