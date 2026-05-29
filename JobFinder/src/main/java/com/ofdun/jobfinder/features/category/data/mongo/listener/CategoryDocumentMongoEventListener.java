package com.ofdun.jobfinder.features.category.data.mongo.listener;

import com.ofdun.jobfinder.common.data.mongo.service.MongoSequenceService;
import com.ofdun.jobfinder.features.category.data.mongo.entity.CategoryDocument;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.springframework.data.mongodb.core.mapping.event.AbstractMongoEventListener;
import org.springframework.data.mongodb.core.mapping.event.BeforeConvertEvent;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class CategoryDocumentMongoEventListener
        extends AbstractMongoEventListener<@NonNull CategoryDocument> {
    private static final String CATEGORIES_SEQUENCE_NAME = "categories_sequence";

    private final MongoSequenceService mongoSequenceService;

    @Override
    public void onBeforeConvert(BeforeConvertEvent<@NonNull CategoryDocument> event) {
        CategoryDocument source = event.getSource();

        if (source.getId() == null) {
            source.setId(mongoSequenceService.generateSequence(CATEGORIES_SEQUENCE_NAME));
        }
    }
}

