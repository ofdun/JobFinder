package com.ofdun.jobfinder.features.education.data.repository;

import static org.junit.jupiter.api.Assertions.*;

import com.ofdun.jobfinder.common.test.MongoRepositoryITBase;
import com.ofdun.jobfinder.features.education.data.mongo.repository.MongoEducationRepository;
import com.ofdun.jobfinder.features.education.domain.model.EducationModel;
import com.ofdun.jobfinder.features.resume.enums.EducationDegree;
import java.time.Year;
import java.util.List;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;

@Tag("mongo")
@ActiveProfiles("test-mongo")
@Import(MongoEducationRepository.class)
class MongoEducationRepositoryIT extends MongoRepositoryITBase {

    @Autowired private MongoEducationRepository mongoEducationRepository;

    @Test
    void createEducations_whenValid_thenReturnsSavedItems() {
        var input =
                List.of(
                        new EducationModel(
                                1L,
                                101L,
                                EducationDegree.BACHELOR,
                                "BMSTU",
                                "IU",
                                "SE",
                                2024),
                        new EducationModel(
                                2L,
                                102L,
                                EducationDegree.MASTER,
                                "MSU",
                                "CS",
                                "AI",
                                2025));

        var saved = mongoEducationRepository.createEducations(input);

        assertEquals(2, saved.size());
    }

    @Test
    void getEducationsByResumeId_whenExists_thenReturnsOnlyRequestedResumeItems() {
        var input =
                List.of(
                        new EducationModel(
                                1L,
                                101L,
                                EducationDegree.BACHELOR,
                                "BMSTU",
                                "IU",
                                "SE",
                                2024),
                        new EducationModel(
                                2L,
                                102L,
                                EducationDegree.MASTER,
                                "MSU",
                                "CS",
                                "AI",
                                2025));
        mongoEducationRepository.createEducations(input);

        var byResume = mongoEducationRepository.getEducationsByResumeId(101L);

        assertEquals(1, byResume.size());
        assertEquals("BMSTU", byResume.get(0).getInstitutionName());
    }

    @Test
    void deleteAllByResumeId_whenExists_thenRemovesItems() {
        var input =
                List.of(
                        new EducationModel(
                                1L,
                                101L,
                                EducationDegree.BACHELOR,
                                "BMSTU",
                                "IU",
                                "SE",
                                2024));
        mongoEducationRepository.createEducations(input);

        mongoEducationRepository.deleteAllByResumeId(101L);

        assertTrue(mongoEducationRepository.getEducationsByResumeId(101L).isEmpty());
    }
}
