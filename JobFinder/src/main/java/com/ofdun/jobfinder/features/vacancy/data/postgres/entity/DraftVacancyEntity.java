package com.ofdun.jobfinder.features.vacancy.data.postgres.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import java.util.Date;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Entity
@Table(schema = "jobfinder", name = "draft_vacancies")
@AllArgsConstructor
@NoArgsConstructor
public class DraftVacancyEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotNull
    @Column(name = "vacancy_id")
    private Long vacancyId;

    @NotNull
    @Column(name = "version_timestamp")
    private Date versionTimestamp;

    @NotNull
    @Column(name = "snapshot", columnDefinition = "text")
    private String snapshot;

    @PrePersist
    protected void onCreate() {
        if (this.versionTimestamp == null) {
            this.versionTimestamp = new Date();
        }
    }
}
