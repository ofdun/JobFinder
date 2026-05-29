package com.ofdun.jobfinder.features.education.data.mongo.listener;

import com.ofdun.jobfinder.common.data.mongo.service.MongoSequenceService;
import com.ofdun.jobfinder.features.education.data.mongo.entity.EducationDocument;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.springframework.data.mongodb.core.mapping.event.AbstractMongoEventListener;
import org.springframework.data.mongodb.core.mapping.event.BeforeConvertEvent;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class EducationDocumentMongoEventListener
        extends AbstractMongoEventListener<@NonNull EducationDocument> {
    private static final String EDUCATIONS_SEQUENCE_NAME = "educations_sequence";

    private final MongoSequenceService mongoSequenceService;

    @Override
    public void onBeforeConvert(BeforeConvertEvent<@NonNull EducationDocument> event) {
        EducationDocument source = event.getSource();

        if (source.getId() == null) {
            source.setId(mongoSequenceService.generateSequence(EDUCATIONS_SEQUENCE_NAME));
        }
    }
}

