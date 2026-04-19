package com.ofdun.jobfinder.features.location.data.repository;

import com.ofdun.jobfinder.features.location.data.entity.LocationEntity;
import lombok.NonNull;
import org.springframework.data.jpa.repository.JpaRepository;

public interface LocationJpaRepository
        extends JpaRepository<@NonNull LocationEntity, @NonNull Long> {}
