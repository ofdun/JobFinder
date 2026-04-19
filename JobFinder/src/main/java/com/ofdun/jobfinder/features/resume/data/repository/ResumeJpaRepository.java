package com.ofdun.jobfinder.features.resume.data.repository;

import com.ofdun.jobfinder.features.resume.data.entity.ResumeEntity;
import lombok.NonNull;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

public interface ResumeJpaRepository
        extends JpaRepository<@NonNull ResumeEntity, @NonNull Long>,
                JpaSpecificationExecutor<ResumeEntity> {}
