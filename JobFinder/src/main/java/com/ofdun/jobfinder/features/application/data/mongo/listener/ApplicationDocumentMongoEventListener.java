package com.ofdun.jobfinder.features.application.data.mongo.listener;

import com.ofdun.jobfinder.common.data.mongo.service.MongoSequenceService;
import com.ofdun.jobfinder.features.application.data.mongo.entity.ApplicationDocument;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.springframework.data.mongodb.core.mapping.event.AbstractMongoEventListener;
import org.springframework.data.mongodb.core.mapping.event.BeforeConvertEvent;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class ApplicationDocumentMongoEventListener
        extends AbstractMongoEventListener<@NonNull ApplicationDocument> {
    private static final String APPLICATIONS_SEQUENCE_NAME = "applications_sequence";

    private final MongoSequenceService mongoSequenceService;

    @Override
    public void onBeforeConvert(BeforeConvertEvent<@NonNull ApplicationDocument> event) {
        ApplicationDocument source = event.getSource();

        if (source.getId() == null) {
            source.setId(mongoSequenceService.generateSequence(APPLICATIONS_SEQUENCE_NAME));
        }
    }
}
