package com.ofdun.jobfinder.features.category.data.postgres.repository;

import com.ofdun.jobfinder.features.category.data.postgres.entity.CategoryEntity;
import java.util.List;
import lombok.NonNull;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CategoryJpaRepository
        extends JpaRepository<@NonNull CategoryEntity, @NonNull Long> {
    List<CategoryEntity> findAllByOrderByNameAsc();
}
