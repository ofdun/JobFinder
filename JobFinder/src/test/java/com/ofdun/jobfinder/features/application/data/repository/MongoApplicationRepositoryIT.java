package com.ofdun.jobfinder.features.application.data.repository;

import static org.junit.jupiter.api.Assertions.*;

import com.ofdun.jobfinder.common.test.MongoRepositoryITBase;
import com.ofdun.jobfinder.features.application.data.mongo.repository.MongoApplicationRepository;
import com.ofdun.jobfinder.features.application.domain.model.ApplicationModel;
import com.ofdun.jobfinder.features.application.domain.repository.ApplicationRepository;
import com.ofdun.jobfinder.features.application.enums.ApplicationStatus;
import java.util.Date;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;

@Tag("mongo")
@ActiveProfiles("test-mongo")
@Import(MongoApplicationRepository.class)
class MongoApplicationRepositoryIT extends MongoRepositoryITBase {

    @Autowired private ApplicationRepository applicationRepository;

    @Test
    void getApplicationsByVacancyId_returnsOnlyTargetVacancy() {
        applicationRepository.createApplication(
                new ApplicationModel(1L, 100L, 11L, new Date(1000L), ApplicationStatus.NEW));
        applicationRepository.createApplication(
                new ApplicationModel(2L, 100L, 12L, new Date(2000L), ApplicationStatus.REJECTION));
        applicationRepository.createApplication(
                new ApplicationModel(3L, 200L, 13L, new Date(3000L), ApplicationStatus.NEW));

        var result = applicationRepository.getApplicationsByVacancyId(100L);

        assertEquals(2, result.size());
        assertTrue(result.stream().allMatch(a -> a.getVacancyId().equals(100L)));
        assertEquals(2L, result.get(0).getId());
    }

    @Test
    void ownApplicationsUseResumeOwnershipAndStablePagination() {
        mongoTemplate.save(
                new com.ofdun.jobfinder.features.resume.data.mongo.entity.ResumeDocument(
                        11L, 7L, 1L, "Own", java.util.List.of(), java.util.List.of(), new Date()));
        mongoTemplate.save(
                new com.ofdun.jobfinder.features.resume.data.mongo.entity.ResumeDocument(
                        12L,
                        8L,
                        1L,
                        "Other",
                        java.util.List.of(),
                        java.util.List.of(),
                        new Date()));
        applicationRepository.createApplication(
                new ApplicationModel(1L, 100L, 11L, new Date(1000), ApplicationStatus.NEW));
        applicationRepository.createApplication(
                new ApplicationModel(2L, 200L, 11L, new Date(1000), ApplicationStatus.NEW));
        applicationRepository.createApplication(
                new ApplicationModel(3L, 100L, 12L, new Date(2000), ApplicationStatus.NEW));
        var first = applicationRepository.getApplicationsByApplicantId(7L, 1, 0);
        var second = applicationRepository.getApplicationsByApplicantId(7L, 1, 1);
        assertEquals(java.util.List.of(2L), first.stream().map(ApplicationModel::getId).toList());
        assertEquals(java.util.List.of(1L), second.stream().map(ApplicationModel::getId).toList());
        assertTrue(applicationRepository.getApplicationsByApplicantId(999L, 20, 0).isEmpty());
    }
}
