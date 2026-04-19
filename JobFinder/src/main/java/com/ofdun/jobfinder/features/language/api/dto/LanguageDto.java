package com.ofdun.jobfinder.features.language.api.dto;

import com.ofdun.jobfinder.features.language.enums.LanguageProficiencyLevel;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class LanguageDto {
    private Long id;

    @NotBlank(message = "Language name cannot be blank")
    private String name;

    @NotNull private LanguageProficiencyLevel proficiencyLevel;
}
