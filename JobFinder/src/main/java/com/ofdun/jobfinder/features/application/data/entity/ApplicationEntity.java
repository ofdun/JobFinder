package com.ofdun.jobfinder.features.application.data.entity;

import com.ofdun.jobfinder.shared.application.enums.ApplicationStatus;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import java.util.Date;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Entity
@Table(schema = "jobfinder", name = "applications")
@AllArgsConstructor
@NoArgsConstructor
public class ApplicationEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotNull private Long vacancyId;

    @NotNull private Long resumeId;

    @NotNull private Date date;

    @NotNull
    @Enumerated(EnumType.STRING)
    private ApplicationStatus status;
}
