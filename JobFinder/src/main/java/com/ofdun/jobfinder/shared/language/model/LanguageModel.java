package com.ofdun.jobfinder.shared.language.model;

import com.ofdun.jobfinder.shared.language.enums.LanguageProficiencyLevel;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class LanguageModel {
    private Long id;

    @NotBlank(message = "Language name cannot be blank")
    private String name;

    @NotNull
    @Enumerated(EnumType.STRING)
    private LanguageProficiencyLevel proficiencyLevel;
}
