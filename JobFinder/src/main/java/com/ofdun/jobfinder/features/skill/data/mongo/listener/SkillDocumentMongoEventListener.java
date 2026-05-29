package com.ofdun.jobfinder.features.skill.data.mongo.listener;

import com.ofdun.jobfinder.common.data.mongo.service.MongoSequenceService;
import com.ofdun.jobfinder.features.skill.data.mongo.entity.SkillDocument;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.springframework.data.mongodb.core.mapping.event.AbstractMongoEventListener;
import org.springframework.data.mongodb.core.mapping.event.BeforeConvertEvent;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class SkillDocumentMongoEventListener extends AbstractMongoEventListener<@NonNull SkillDocument> {
    private static final String SKILLS_SEQUENCE_NAME = "skills_sequence";

    private final MongoSequenceService mongoSequenceService;

    @Override
    public void onBeforeConvert(BeforeConvertEvent<@NonNull SkillDocument> event) {
        SkillDocument source = event.getSource();

        if (source.getId() == null) {
            source.setId(mongoSequenceService.generateSequence(SKILLS_SEQUENCE_NAME));
        }
    }
}

