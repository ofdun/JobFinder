package com.ofdun.jobfinder.features.category.api.controller;

import com.ofdun.jobfinder.features.category.api.dto.CategoryDto;
import com.ofdun.jobfinder.features.category.api.mapper.CategoryApiMapper;
import com.ofdun.jobfinder.features.category.domain.service.CategoryService;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/categories")
@RequiredArgsConstructor
public class CategoryController {
    private final CategoryService categoryService;

    @GetMapping
    public ResponseEntity<List<CategoryDto>> getCategories() {
        var categories =
                categoryService.getAllCategories().stream().map(CategoryApiMapper::toDto).toList();
        return ResponseEntity.ok(categories);
    }
}

