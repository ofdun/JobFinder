package com.ofdun.jobfinder.features.category.data.repository;

import com.ofdun.jobfinder.features.category.data.entity.CategoryEntity;
import java.util.List;
import lombok.NonNull;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CategoryJpaRepository
        extends JpaRepository<@NonNull CategoryEntity, @NonNull Long> {
    List<CategoryEntity> findAllByOrderByNameAsc();
}
