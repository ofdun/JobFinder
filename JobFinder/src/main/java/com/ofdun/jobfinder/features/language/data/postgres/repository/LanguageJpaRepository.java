package com.ofdun.jobfinder.features.language.data.postgres.repository;

import com.ofdun.jobfinder.features.language.data.postgres.entity.LanguageEntity;
import java.util.List;
import lombok.NonNull;
import org.springframework.data.jpa.repository.JpaRepository;

public interface LanguageJpaRepository
        extends JpaRepository<@NonNull LanguageEntity, @NonNull Long> {
    List<LanguageEntity> findAllByOrderByNameAsc();
}
