package com.ofdun.jobfinder.shared.skill.model;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class SkillModel {
    private Long id;

    @NotBlank private String name;
}
