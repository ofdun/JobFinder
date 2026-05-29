package com.ofdun.jobfinder.features.resume.data.repository;

import static org.junit.jupiter.api.Assertions.*;

import com.ofdun.jobfinder.common.test.MongoRepositoryITBase;
import com.ofdun.jobfinder.features.education.data.mongo.repository.MongoEducationRepository;
import com.ofdun.jobfinder.features.experience.data.mongo.repository.MongoJobExperienceRepository;
import com.ofdun.jobfinder.features.resume.data.mongo.repository.MongoResumeCRUDRepository;
import com.ofdun.jobfinder.features.resume.data.mongo.repository.MongoResumeRepository;
import com.ofdun.jobfinder.features.resume.domain.model.ResumeModel;
import com.ofdun.jobfinder.features.resume.domain.model.ResumeSearchFilter;
import java.util.Date;
import java.util.List;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;

@Tag("mongo")
@ActiveProfiles("test-mongo")
@Import({
    MongoResumeRepository.class,
    MongoEducationRepository.class,
    MongoJobExperienceRepository.class
})
class MongoResumeRepositoryIT extends MongoRepositoryITBase {

    @Autowired private MongoResumeRepository mongoResumeRepository;
    @Autowired private MongoResumeCRUDRepository mongoResumeCRUDRepository;

    @Test
    void searchResumes_filtersByCategoryAndSkills() {
        mongoResumeCRUDRepository.save(
                new com.ofdun.jobfinder.features.resume.data.mongo.entity.ResumeDocument(
                        1L,
                        10L,
                        2L,
                        "Java backend",
                        List.of(1L, 2L),
                        List.of(1L),
                        new Date(1000L)));
        mongoResumeCRUDRepository.save(
                new com.ofdun.jobfinder.features.resume.data.mongo.entity.ResumeDocument(
                        2L,
                        11L,
                        3L,
                        "Frontend",
                        List.of(3L),
                        List.of(2L),
                        new Date(2000L)));

        var result =
                mongoResumeRepository.searchResumes(
                        new ResumeSearchFilter("java", null, 2L, null, null, List.of(1L), null),
                        10,
                        0,
                        "id",
                        false);

        assertEquals(1, result.getItems().size());
        assertEquals(1L, result.getItems().get(0).getId());
    }

    @Test
    void updateResume_updatesDocument() {
        mongoResumeCRUDRepository.save(
                new com.ofdun.jobfinder.features.resume.data.mongo.entity.ResumeDocument(
                        5L,
                        33L,
                        4L,
                        "Old",
                        List.of(1L),
                        List.of(1L),
                        new Date()));

        var updated =
                mongoResumeRepository.updateResume(
                        new ResumeModel(
                                5L,
                                33L,
                                4L,
                                "New",
                                List.of(1L, 2L),
                                null,
                                null,
                                List.of(1L),
                                new Date()));

        assertEquals("New", updated.getDescription());
        assertEquals(2, updated.getSkillIds().size());
    }
}
