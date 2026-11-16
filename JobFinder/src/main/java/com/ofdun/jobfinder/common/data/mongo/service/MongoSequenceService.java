package com.ofdun.jobfinder.common.data.mongo.service;

import com.ofdun.jobfinder.common.data.mongo.entity.DatabaseSequence;
import io.micrometer.observation.annotation.Observed;
import lombok.RequiredArgsConstructor;
import org.springframework.data.mongodb.core.FindAndModifyOptions;
import org.springframework.data.mongodb.core.MongoOperations;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class MongoSequenceService {
    private final MongoOperations mongoOperations;

    @Observed(
            name = "jobfinder.sequence.repository.generateSequence",
            contextualName = "MongoDB: выделение идентификатора MongoDB",
            lowCardinalityKeyValues = {
                "feature",
                "sequence",
                "layer",
                "repository",
                "storage",
                "MongoDB"
            })
    public long generateSequence(String sequenceName) {
        Query query = new Query(Criteria.where("_id").is(sequenceName));
        Update update = new Update().inc("seq", 1);
        FindAndModifyOptions options = FindAndModifyOptions.options().upsert(true).returnNew(true);

        DatabaseSequence counter =
                mongoOperations.findAndModify(query, update, options, DatabaseSequence.class);

        if (counter == null) {
            throw new IllegalStateException(
                    "Failed to generate Mongo sequence for " + sequenceName);
        }

        return counter.getSeq();
    }
}
