package com.ofdun.jobfinder.features.language.data.repository;

import static org.junit.jupiter.api.Assertions.*;

import com.ofdun.jobfinder.common.test.MongoRepositoryITBase;
import com.ofdun.jobfinder.features.language.data.mongo.entity.LanguageDocument;
import com.ofdun.jobfinder.features.language.data.mongo.repository.MongoLanguageCRUDRepository;
import com.ofdun.jobfinder.features.language.data.mongo.repository.MongoLanguageRepository;
import com.ofdun.jobfinder.features.language.domain.repository.LanguageRepository;
import com.ofdun.jobfinder.features.language.enums.LanguageProficiencyLevel;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;

@Tag("mongo")
@ActiveProfiles("test-mongo")
@Import(MongoLanguageRepository.class)
class MongoLanguageRepositoryIT extends MongoRepositoryITBase {

    @Autowired private LanguageRepository languageRepository;
    @Autowired private MongoLanguageCRUDRepository mongoLanguageCRUDRepository;

    @Test
    void getLanguageById_whenExists_thenFound() {
        mongoLanguageCRUDRepository.save(
                new LanguageDocument(10L, "English", LanguageProficiencyLevel.C1));

        var found = languageRepository.getLanguageById(10L);

        assertTrue(found.isPresent());
        assertEquals("English", found.get().getName());
    }
}
