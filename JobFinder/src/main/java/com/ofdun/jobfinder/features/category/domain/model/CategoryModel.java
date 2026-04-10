package com.ofdun.jobfinder.features.category.domain.model;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class CategoryModel {
    private Long id;

    @NotBlank(message = "Category name is required")
    private String name;
}
