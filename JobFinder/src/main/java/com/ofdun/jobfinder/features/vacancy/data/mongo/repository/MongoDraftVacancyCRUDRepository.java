package com.ofdun.jobfinder.features.vacancy.data.mongo.repository;

import com.ofdun.jobfinder.features.vacancy.data.mongo.entity.DraftVacancyDocument;
import java.util.List;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface MongoDraftVacancyCRUDRepository
        extends MongoRepository<DraftVacancyDocument, Long> {
    List<DraftVacancyDocument> findAllByVacancyIdOrderByVersionTimestampDesc(Long vacancyId);
}

