package com.ofdun.jobfinder.features.applicant.data.mongo.listener;

import com.ofdun.jobfinder.common.data.mongo.service.MongoSequenceService;
import com.ofdun.jobfinder.features.applicant.data.mongo.entity.ApplicantDocument;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.springframework.data.mongodb.core.mapping.event.AbstractMongoEventListener;
import org.springframework.data.mongodb.core.mapping.event.BeforeConvertEvent;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class ApplicantDocumentMongoEventListener
        extends AbstractMongoEventListener<@NonNull ApplicantDocument> {
    private static final String APPLICANTS_SEQUENCE_NAME = "applicants_sequence";

    private final MongoSequenceService mongoSequenceService;

    @Override
    public void onBeforeConvert(BeforeConvertEvent<@NonNull ApplicantDocument> event) {
        ApplicantDocument source = event.getSource();

        if (source.getId() == null) {
            source.setId(mongoSequenceService.generateSequence(APPLICANTS_SEQUENCE_NAME));
        }
    }
}
