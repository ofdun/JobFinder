package com.ofdun.jobfinder.features.experience.data.mongo.listener;

import com.ofdun.jobfinder.common.data.mongo.service.MongoSequenceService;
import com.ofdun.jobfinder.features.experience.data.mongo.entity.JobExperienceDocument;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.springframework.data.mongodb.core.mapping.event.AbstractMongoEventListener;
import org.springframework.data.mongodb.core.mapping.event.BeforeConvertEvent;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class JobExperienceDocumentMongoEventListener
        extends AbstractMongoEventListener<@NonNull JobExperienceDocument> {
    private static final String EXPERIENCES_SEQUENCE_NAME = "experiences_sequence";

    private final MongoSequenceService mongoSequenceService;

    @Override
    public void onBeforeConvert(BeforeConvertEvent<@NonNull JobExperienceDocument> event) {
        JobExperienceDocument source = event.getSource();

        if (source.getId() == null) {
            source.setId(mongoSequenceService.generateSequence(EXPERIENCES_SEQUENCE_NAME));
        }
    }
}

