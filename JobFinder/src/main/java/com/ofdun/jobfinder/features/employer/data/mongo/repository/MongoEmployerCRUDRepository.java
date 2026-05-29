package com.ofdun.jobfinder.features.employer.data.mongo.repository;

import com.ofdun.jobfinder.features.employer.data.mongo.entity.EmployerDocument;
import java.util.Optional;
import lombok.NonNull;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface MongoEmployerCRUDRepository
        extends MongoRepository<@NonNull EmployerDocument, @NonNull Long> {
    Optional<EmployerDocument> findByEmail(String email);
}

