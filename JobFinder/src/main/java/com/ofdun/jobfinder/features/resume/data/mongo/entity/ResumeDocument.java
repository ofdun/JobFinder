package com.ofdun.jobfinder.features.resume.data.mongo.entity;

import jakarta.validation.constraints.NotNull;
import java.util.Date;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@Data
@Document(collection = "resumes")
@AllArgsConstructor
@NoArgsConstructor
public class ResumeDocument {
    @Id
    private Long id;

    @NotNull private Long applicantId;

    @NotNull private Long categoryId;

    private String description;

    private List<@NotNull Long> skillIds;

    private List<@NotNull Long> languages;

    @NotNull private Date creationDate;
}

