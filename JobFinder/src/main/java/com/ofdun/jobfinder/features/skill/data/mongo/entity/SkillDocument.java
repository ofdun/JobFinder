package com.ofdun.jobfinder.features.skill.data.mongo.entity;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@Data
@Document(collection = "skills")
@AllArgsConstructor
@NoArgsConstructor
public class SkillDocument {
    @Id
    private Long id;

    @NotBlank private String name;
}

