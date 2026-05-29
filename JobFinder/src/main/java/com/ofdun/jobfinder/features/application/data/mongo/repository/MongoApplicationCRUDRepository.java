package com.ofdun.jobfinder.features.application.data.mongo.repository;

import com.ofdun.jobfinder.features.application.data.mongo.entity.ApplicationDocument;
import java.util.List;
import lombok.NonNull;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface MongoApplicationCRUDRepository
        extends MongoRepository<@NonNull ApplicationDocument, @NonNull Long> {
    List<ApplicationDocument> findByVacancyIdOrderByApplicationDateDesc(Long vacancyId);
}

