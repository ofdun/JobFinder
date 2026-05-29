package com.ofdun.jobfinder.features.applicant.data.mongo.repository;

import com.ofdun.jobfinder.features.applicant.data.mongo.entity.ApplicantDocument;
import lombok.NonNull;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.Optional;

public interface MongoApplicantCRUDRepository extends MongoRepository<@NonNull ApplicantDocument, @NonNull Long> {
    Optional<ApplicantDocument> findByEmail(String email);
}
