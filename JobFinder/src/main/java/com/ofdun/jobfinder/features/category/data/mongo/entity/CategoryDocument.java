package com.ofdun.jobfinder.features.category.data.mongo.entity;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@Data
@Document(collection = "categories")
@AllArgsConstructor
@NoArgsConstructor
public class CategoryDocument {
    @Id
    private Long id;

    @NotBlank(message = "Category name is required")
    private String name;
}

