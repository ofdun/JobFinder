package com.ofdun.jobfinder.features.resume.data.entity;

import com.ofdun.jobfinder.shared.category.entity.CategoryEntity;
import com.ofdun.jobfinder.shared.education.entity.EducationEntity;
import com.ofdun.jobfinder.shared.experience.entity.JobExperienceEntity;
import com.ofdun.jobfinder.shared.language.entity.LanguageEntity;
import com.ofdun.jobfinder.shared.skill.entity.SkillEntity;
import jakarta.persistence.*;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
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

    @Valid
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "category_id", nullable = false)
    private CategoryEntity category;

    private String description;

    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(
            schema = "jobfinder",
            name = "resume_skills",
            joinColumns = @JoinColumn(name = "resume_id"),
            inverseJoinColumns = @JoinColumn(name = "skill_id"))
    private List<@NotBlank @Valid SkillEntity> skills;

    @OneToMany(fetch = FetchType.LAZY, cascade = CascadeType.ALL, mappedBy = "resumeId")
    private List<@NotBlank @Valid EducationEntity> educations;

    @OneToMany(fetch = FetchType.LAZY, cascade = CascadeType.ALL, mappedBy = "resumeId")
    private List<@NotBlank @Valid JobExperienceEntity> jobExperiences;

    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(
            schema = "jobfinder",
            name = "language_resume",
            joinColumns = @JoinColumn(name = "resume_id"),
            inverseJoinColumns = @JoinColumn(name = "language_id"))
    private List<@NotBlank @Valid LanguageEntity> languages;

    @NotNull private Date creationDate;
}
