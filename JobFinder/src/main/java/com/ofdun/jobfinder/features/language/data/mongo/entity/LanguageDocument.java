package com.ofdun.jobfinder.features.language.data.mongo.entity;

import com.ofdun.jobfinder.features.language.enums.LanguageProficiencyLevel;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Document(collection = "languages")
public class LanguageDocument {
    @Id
    private Long id;

    @NotBlank(message = "Language name cannot be blank")
    private String name;

    @NotNull private LanguageProficiencyLevel proficiencyLevel;
}

