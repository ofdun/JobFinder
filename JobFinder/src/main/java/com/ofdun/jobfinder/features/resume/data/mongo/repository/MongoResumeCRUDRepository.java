package com.ofdun.jobfinder.features.resume.data.mongo.repository;

import com.ofdun.jobfinder.features.resume.data.mongo.entity.ResumeDocument;
import lombok.NonNull;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface MongoResumeCRUDRepository
        extends MongoRepository<@NonNull ResumeDocument, @NonNull Long> {}

