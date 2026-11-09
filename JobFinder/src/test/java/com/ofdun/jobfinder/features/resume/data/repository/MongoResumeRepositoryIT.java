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
    com.ofdun.jobfinder.common.data.mongo.service.MongoSequenceService.class,
    com.ofdun.jobfinder.features.resume.data.mongo.listener.ResumeDocumentMongoEventListener.class,
    com.ofdun.jobfinder.features.education.data.mongo.listener.EducationDocumentMongoEventListener
            .class,
    com.ofdun.jobfinder.features.experience.data.mongo.listener
            .JobExperienceDocumentMongoEventListener.class,
    MongoResumeRepository.class,
    MongoEducationRepository.class,
    MongoJobExperienceRepository.class
})
class MongoResumeRepositoryIT extends MongoRepositoryITBase implements ResumePersistenceContract {

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
                        2L, 11L, 3L, "Frontend", List.of(3L), List.of(2L), new Date(2000L)));

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
                        5L, 33L, 4L, "Old", List.of(1L), List.of(1L), new Date()));

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

    @Override
    public com.ofdun.jobfinder.features.resume.domain.repository.RelationalResumeRepository
            repository() {
        return mongoResumeRepository;
    }

    @Override
    public void assertNoChildren(long id) {
        var query =
                org.springframework.data.mongodb.core.query.Query.query(
                        org.springframework.data.mongodb.core.query.Criteria.where("resumeId")
                                .is(id));
        assertEquals(0L, mongoTemplate.count(query, "educations"));
        assertEquals(0L, mongoTemplate.count(query, "experiences"));
    }

    @Test
    @Override
    public void nestedRecordsRoundTripWithCurrentEmployment() {
        ResumePersistenceContract.super.nestedRecordsRoundTripWithCurrentEmployment();
    }

    @Test
    @Override
    public void replacingWithEmptyCollectionsRemovesStoredRelations() {
        ResumePersistenceContract.super.replacingWithEmptyCollectionsRemovesStoredRelations();
    }

    @Test
    @Override
    public void replacingChildrenDoesNotAppendOrModifyAnotherResume() {
        ResumePersistenceContract.super.replacingChildrenDoesNotAppendOrModifyAnotherResume();
    }

    @Test
    @Override
    public void deletingResumeRemovesItsChildrenOnly() {
        ResumePersistenceContract.super.deletingResumeRemovesItsChildrenOnly();
    }

    @Test
    @Override
    public void paginationWithEqualDatesHasStableOrderAndFullCount() {
        ResumePersistenceContract.super.paginationWithEqualDatesHasStableOrderAndFullCount();
    }

    @Test
    @Override
    public void ownershipFilterCannotReturnOtherApplicantsResumes() {
        ResumePersistenceContract.super.ownershipFilterCannotReturnOtherApplicantsResumes();
    }
}
