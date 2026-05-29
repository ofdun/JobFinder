package com.ofdun.jobfinder.features.vacancy.data.mongo.listener;

import com.ofdun.jobfinder.common.data.mongo.service.MongoSequenceService;
import com.ofdun.jobfinder.features.vacancy.data.mongo.entity.VacancyDocument;
import com.ofdun.jobfinder.features.vacancy.enums.VacancyStatus;
import java.util.Date;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.springframework.data.mongodb.core.mapping.event.AbstractMongoEventListener;
import org.springframework.data.mongodb.core.mapping.event.BeforeConvertEvent;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class VacancyDocumentMongoEventListener
        extends AbstractMongoEventListener<@NonNull VacancyDocument> {
    private static final String VACANCIES_SEQUENCE_NAME = "vacancies_sequence";

    private final MongoSequenceService mongoSequenceService;

    @Override
    public void onBeforeConvert(BeforeConvertEvent<@NonNull VacancyDocument> event) {
        VacancyDocument source = event.getSource();

        if (source.getId() == null) {
            source.setId(mongoSequenceService.generateSequence(VACANCIES_SEQUENCE_NAME));
        }

        if (source.getPublicationDate() == null) {
            source.setPublicationDate(new Date());
        }

        if (source.getStatus() == null) {
            source.setStatus(VacancyStatus.ACTIVE);
        }
    }
}

