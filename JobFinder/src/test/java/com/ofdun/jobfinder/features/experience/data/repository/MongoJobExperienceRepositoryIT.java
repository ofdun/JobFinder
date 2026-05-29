package com.ofdun.jobfinder.features.experience.data.repository;

import static org.junit.jupiter.api.Assertions.*;

import com.ofdun.jobfinder.common.test.MongoRepositoryITBase;
import com.ofdun.jobfinder.features.experience.data.mongo.repository.MongoJobExperienceRepository;
import com.ofdun.jobfinder.features.experience.domain.model.JobExperienceModel;
import java.time.LocalDate;
import java.util.List;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;

@Tag("mongo")
@ActiveProfiles("test-mongo")
@Import(MongoJobExperienceRepository.class)
class MongoJobExperienceRepositoryIT extends MongoRepositoryITBase {

    @Autowired private MongoJobExperienceRepository mongoJobExperienceRepository;

    @Test
    void createJobExperiences_whenValid_thenReturnsSavedItems() {
        var input =
                List.of(
                        new JobExperienceModel(
                                1L,
                                201L,
                                "Dev",
                                "Yandex",
                                "desc",
                                LocalDate.of(2023, 1, 1),
                                LocalDate.of(2024, 1, 1)),
                        new JobExperienceModel(
                                2L,
                                202L,
                                "QA",
                                "VK",
                                "desc2",
                                LocalDate.of(2022, 1, 1),
                                LocalDate.of(2023, 1, 1)));

        var saved = mongoJobExperienceRepository.createJobExperiences(input);

        assertEquals(2, saved.size());
    }

    @Test
    void getJobExperiencesByResumeId_whenExists_thenReturnsOnlyRequestedResumeItems() {
        var input =
                List.of(
                        new JobExperienceModel(
                                1L,
                                201L,
                                "Dev",
                                "Yandex",
                                "desc",
                                LocalDate.of(2023, 1, 1),
                                LocalDate.of(2024, 1, 1)),
                        new JobExperienceModel(
                                2L,
                                202L,
                                "QA",
                                "VK",
                                "desc2",
                                LocalDate.of(2022, 1, 1),
                                LocalDate.of(2023, 1, 1)));
        mongoJobExperienceRepository.createJobExperiences(input);

        var byResume = mongoJobExperienceRepository.getJobExperiencesByResumeId(201L);

        assertEquals(1, byResume.size());
        assertEquals("Dev", byResume.get(0).getPosition());
    }

    @Test
    void deleteAllByResumeId_whenExists_thenRemovesItems() {
        var input =
                List.of(
                        new JobExperienceModel(
                                1L,
                                201L,
                                "Dev",
                                "Yandex",
                                "desc",
                                LocalDate.of(2023, 1, 1),
                                LocalDate.of(2024, 1, 1)));
        mongoJobExperienceRepository.createJobExperiences(input);

        mongoJobExperienceRepository.deleteAllByResumeId(201L);

        assertTrue(mongoJobExperienceRepository.getJobExperiencesByResumeId(201L).isEmpty());
    }
}
