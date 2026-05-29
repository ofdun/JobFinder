package com.ofdun.jobfinder.features.location.data.mongo.listener;

import com.ofdun.jobfinder.common.data.mongo.service.MongoSequenceService;
import com.ofdun.jobfinder.features.location.data.mongo.entity.LocationDocument;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.springframework.data.mongodb.core.mapping.event.AbstractMongoEventListener;
import org.springframework.data.mongodb.core.mapping.event.BeforeConvertEvent;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class LocationDocumentMongoEventListener
        extends AbstractMongoEventListener<@NonNull LocationDocument> {
    private static final String LOCATIONS_SEQUENCE_NAME = "locations_sequence";

    private final MongoSequenceService mongoSequenceService;

    @Override
    public void onBeforeConvert(BeforeConvertEvent<@NonNull LocationDocument> event) {
        LocationDocument source = event.getSource();

        if (source.getId() == null) {
            source.setId(mongoSequenceService.generateSequence(LOCATIONS_SEQUENCE_NAME));
        }
    }
}

