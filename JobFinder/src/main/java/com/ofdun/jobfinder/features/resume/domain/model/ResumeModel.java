package com.ofdun.jobfinder.features.resume.domain.model;

import com.ofdun.jobfinder.features.education.domain.model.EducationModel;
import com.ofdun.jobfinder.features.experience.domain.model.JobExperienceModel;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import java.util.Date;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

@Data
@ToString
@AllArgsConstructor
@NoArgsConstructor
public class ResumeModel {
    public ResumeModel(Long id) {
        this.id = id;
    }

    public ResumeModel(
            Long id,
            Long applicantId,
            Long categoryId,
            String description,
            List<Long> skillIds,
            List<EducationModel> educations,
            List<JobExperienceModel> jobExperiences,
            List<Long> languageIds,
            Date publishedDate) {
        this.id = id;
        this.applicantId = applicantId;
        this.categoryId = categoryId;
        this.description = description;
        this.skillIds = skillIds;
        this.educations = educations;
        this.jobExperiences = jobExperiences;
        this.languageIds = languageIds;
        this.date = publishedDate;
    }

    private Long id;

    @NotNull private Long applicantId;

    @NotNull private Long categoryId;

    private String description;

    private List<@NotNull @Valid Long> skillIds;
    private List<@NotNull @Valid EducationModel> educations;
    private List<@NotNull @Valid JobExperienceModel> jobExperiences;
    private List<@NotNull @Valid Long> languageIds;

    @NotNull private Date date;

    private List<@NotNull Float> embedding;
}
