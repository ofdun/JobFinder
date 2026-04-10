package com.ofdun.jobfinder.features.resume.data.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import java.util.Date;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Entity
@Table(schema = "jobfinder", name = "resumes")
@AllArgsConstructor
@NoArgsConstructor
public class ResumeEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotNull private Long applicantId;

    @NotNull private Long categoryId;

    private String description;

    @ElementCollection
    @CollectionTable(schema = "jobfinder", name = "resume_skills", joinColumns = @JoinColumn(name = "resume_id"))
    @Column(name = "skill_id")
    private List<@NotNull Long> skillIds;

    @ElementCollection
    @CollectionTable(schema = "jobfinder", name = "language_resume", joinColumns = @JoinColumn(name = "resume_id"))
    @Column(name = "language_id")
    private List<@NotNull Long> languages;

    @NotNull private Date creationDate;

    @PrePersist
    protected void onCreate() {
        this.creationDate = new Date();
    }
}