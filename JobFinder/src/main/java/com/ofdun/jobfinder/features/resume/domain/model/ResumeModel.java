package com.ofdun.jobfinder.features.resume.domain.model;

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
    private Long seekerId;
    private String category;
    private String description;
    private List<String> skills;
    private List<EducationModel> educations;
    private List<JobExperience> jobExperiences;
    private Date publishedDate;

    private List<Double> embedding;
}
