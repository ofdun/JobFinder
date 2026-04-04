package com.ofdun.jobfinder.features.resume.domain.model;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.ToString;

import java.util.Date;
import java.util.List;

@AllArgsConstructor
@Data
@ToString
public class ResumeModel {
    public ResumeModel(Long id) {
        this.id = id;
    }

    private Long id;

    @NotNull
    private Long seekerId;

    @NotBlank
    private String category;

    private String description;

    private List<@NotBlank String> skills;
    private List<@Valid EducationModel> educations;
    private List<@Valid JobExperience> jobExperiences;

    @NotNull
    private Date publishedDate;

    private List<Double> embedding;
}
