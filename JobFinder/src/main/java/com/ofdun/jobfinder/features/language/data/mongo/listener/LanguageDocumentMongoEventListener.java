package com.ofdun.jobfinder.features.language.data.mongo.listener;

import com.ofdun.jobfinder.common.data.mongo.service.MongoSequenceService;
import com.ofdun.jobfinder.features.language.data.mongo.entity.LanguageDocument;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.springframework.data.mongodb.core.mapping.event.AbstractMongoEventListener;
import org.springframework.data.mongodb.core.mapping.event.BeforeConvertEvent;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class LanguageDocumentMongoEventListener
        extends AbstractMongoEventListener<@NonNull LanguageDocument> {
    private static final String LANGUAGES_SEQUENCE_NAME = "languages_sequence";

    private final MongoSequenceService mongoSequenceService;

    @Override
    public void onBeforeConvert(BeforeConvertEvent<@NonNull LanguageDocument> event) {
        LanguageDocument source = event.getSource();

        if (source.getId() == null) {
            source.setId(mongoSequenceService.generateSequence(LANGUAGES_SEQUENCE_NAME));
        }
    }
}

