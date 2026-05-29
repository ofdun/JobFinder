package com.ofdun.jobfinder.features.experience.data.postgres.repository;

import com.ofdun.jobfinder.features.experience.data.postgres.entity.JobExperienceEntity;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface JobExperienceJpaRepository extends JpaRepository<JobExperienceEntity, Long> {
    List<JobExperienceEntity> findAllByResumeId(Long resumeId);

    void deleteAllByResumeId(Long resumeId);
}
