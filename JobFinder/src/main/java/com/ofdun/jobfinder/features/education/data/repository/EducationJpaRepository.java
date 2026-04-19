package com.ofdun.jobfinder.features.education.data.repository;

import com.ofdun.jobfinder.features.education.data.entity.EducationEntity;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface EducationJpaRepository extends JpaRepository<EducationEntity, Long> {
    List<EducationEntity> findAllByResumeId(Long resumeId);

    void deleteAllByResumeId(Long resumeId);
}
