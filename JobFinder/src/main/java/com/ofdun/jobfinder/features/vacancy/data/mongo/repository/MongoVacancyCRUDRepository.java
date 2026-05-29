package com.ofdun.jobfinder.features.vacancy.data.mongo.repository;

import com.ofdun.jobfinder.features.vacancy.data.mongo.entity.VacancyDocument;
import lombok.NonNull;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface MongoVacancyCRUDRepository
        extends MongoRepository<@NonNull VacancyDocument, @NonNull Long> {}

