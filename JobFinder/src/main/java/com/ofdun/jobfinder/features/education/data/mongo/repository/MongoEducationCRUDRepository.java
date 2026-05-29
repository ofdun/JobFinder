package com.ofdun.jobfinder.features.education.data.mongo.repository;

import com.ofdun.jobfinder.features.education.data.mongo.entity.EducationDocument;
import java.util.List;
import lombok.NonNull;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface MongoEducationCRUDRepository
        extends MongoRepository<@NonNull EducationDocument, @NonNull Long> {
    List<EducationDocument> findAllByResumeId(Long resumeId);

    void deleteAllByResumeId(Long resumeId);
}

