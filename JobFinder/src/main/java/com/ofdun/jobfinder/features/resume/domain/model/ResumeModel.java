package com.ofdun.jobfinder.features.resume.domain.model;

import com.ofdun.jobfinder.shared.category.model.CategoryModel;
import com.ofdun.jobfinder.shared.education.model.EducationModel;
import com.ofdun.jobfinder.shared.experience.model.JobExperienceModel;
import com.ofdun.jobfinder.shared.language.model.LanguageModel;
import com.ofdun.jobfinder.shared.skill.model.SkillModel;
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
            CategoryModel category,
            String description,
            List<SkillModel> skills,
            List<EducationModel> educations,
            List<JobExperienceModel> jobExperiences,
            List<LanguageModel> languages,
            Date publishedDate) {
        this.id = id;
        this.applicantId = applicantId;
        this.category = category;
        this.description = description;
        this.skills = skills;
        this.educations = educations;
        this.jobExperiences = jobExperiences;
        this.languages = languages;
        this.date = publishedDate;
    }

    private Long id;

    @NotNull private Long applicantId;

    @NotNull @Valid private CategoryModel category;

    private String description;

    private List<@NotNull @Valid SkillModel> skills;
    private List<@NotNull @Valid EducationModel> educations;
    private List<@NotNull @Valid JobExperienceModel> jobExperiences;
    private List<@NotNull @Valid LanguageModel> languages;

    @NotNull private Date date;

    private List<@NotNull Float> embedding;
}
