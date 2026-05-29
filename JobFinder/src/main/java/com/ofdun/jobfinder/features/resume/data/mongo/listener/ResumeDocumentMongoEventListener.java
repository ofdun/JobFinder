package com.ofdun.jobfinder.features.resume.data.mongo.listener;

import com.ofdun.jobfinder.common.data.mongo.service.MongoSequenceService;
import com.ofdun.jobfinder.features.resume.data.mongo.entity.ResumeDocument;
import java.util.Date;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.springframework.data.mongodb.core.mapping.event.AbstractMongoEventListener;
import org.springframework.data.mongodb.core.mapping.event.BeforeConvertEvent;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class ResumeDocumentMongoEventListener extends AbstractMongoEventListener<@NonNull ResumeDocument> {
    private static final String RESUMES_SEQUENCE_NAME = "resumes_sequence";

    private final MongoSequenceService mongoSequenceService;

    @Override
    public void onBeforeConvert(BeforeConvertEvent<@NonNull ResumeDocument> event) {
        ResumeDocument source = event.getSource();

        if (source.getId() == null) {
            source.setId(mongoSequenceService.generateSequence(RESUMES_SEQUENCE_NAME));
        }

        if (source.getCreationDate() == null) {
            source.setCreationDate(new Date());
        }
    }
}

