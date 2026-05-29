package com.ofdun.jobfinder.features.experience.data.mongo.repository;

import com.ofdun.jobfinder.features.experience.data.mongo.entity.JobExperienceDocument;
import java.util.List;
import lombok.NonNull;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface MongoJobExperienceCRUDRepository
        extends MongoRepository<@NonNull JobExperienceDocument, @NonNull Long> {
    List<JobExperienceDocument> findAllByResumeId(Long resumeId);

    void deleteAllByResumeId(Long resumeId);
}

