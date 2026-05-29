package com.ofdun.jobfinder.features.employer.data.mongo.listener;

import com.ofdun.jobfinder.common.data.mongo.service.MongoSequenceService;
import com.ofdun.jobfinder.features.employer.data.mongo.entity.EmployerDocument;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.springframework.data.mongodb.core.mapping.event.AbstractMongoEventListener;
import org.springframework.data.mongodb.core.mapping.event.BeforeConvertEvent;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class EmployerDocumentMongoEventListener
        extends AbstractMongoEventListener<@NonNull EmployerDocument> {
    private static final String EMPLOYERS_SEQUENCE_NAME = "employers_sequence";

    private final MongoSequenceService mongoSequenceService;

    @Override
    public void onBeforeConvert(BeforeConvertEvent<@NonNull EmployerDocument> event) {
        EmployerDocument source = event.getSource();

        if (source.getId() == null) {
            source.setId(mongoSequenceService.generateSequence(EMPLOYERS_SEQUENCE_NAME));
        }
    }
}

