package com.ofdun.jobfinder.features.vacancy.data.mongo.listener;

import com.ofdun.jobfinder.common.data.mongo.service.MongoSequenceService;
import com.ofdun.jobfinder.features.vacancy.data.mongo.entity.DraftVacancyDocument;
import java.util.Date;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.springframework.data.mongodb.core.mapping.event.AbstractMongoEventListener;
import org.springframework.data.mongodb.core.mapping.event.BeforeConvertEvent;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class DraftVacancyDocumentMongoEventListener
        extends AbstractMongoEventListener<@NonNull DraftVacancyDocument> {
    private static final String DRAFT_VACANCIES_SEQUENCE_NAME = "draft_vacancies_sequence";

    private final MongoSequenceService mongoSequenceService;

    @Override
    public void onBeforeConvert(BeforeConvertEvent<@NonNull DraftVacancyDocument> event) {
        DraftVacancyDocument source = event.getSource();

        if (source.getId() == null) {
            source.setId(mongoSequenceService.generateSequence(DRAFT_VACANCIES_SEQUENCE_NAME));
        }

        if (source.getVersionTimestamp() == null) {
            source.setVersionTimestamp(new Date());
        }
    }
}

