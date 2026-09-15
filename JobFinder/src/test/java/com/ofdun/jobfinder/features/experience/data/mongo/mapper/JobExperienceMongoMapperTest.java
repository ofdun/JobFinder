package com.ofdun.jobfinder.features.experience.data.mongo.mapper;

import static com.ofdun.jobfinder.support.TestDataMother.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import com.ofdun.jobfinder.features.experience.data.mongo.entity.JobExperienceDocument;
import java.util.*;
import org.junit.jupiter.api.*;
import org.mockito.*;

class JobExperienceMongoMapperTest {

    @Test
    @Tag("equivalence")
    void toEntity_whenPresent_mapsAllFields() {
        var model = experience();

        var result = JobExperienceMongoMapper.toEntity(model);

        assertEquals(model.getId(), result.getId());
        assertEquals(model.getResumeId(), result.getResumeId());
        assertEquals(model.getPosition(), result.getPosition());
        assertEquals(model.getCompanyName(), result.getCompanyName());
        assertEquals(model.getDescription(), result.getDescription());
        assertEquals(model.getStartDate(), result.getStartDate());
        assertEquals(model.getEndDate(), result.getEndDate());
    }

    @Test
    @Tag("equivalence")
    void toEntity_whenNull_returnsNull() {

        var result = JobExperienceMongoMapper.toEntity(null);

        assertNull(result);
    }

    @Test
    @Tag("equivalence")
    void toModel_whenPresent_mapsAllFields() {
        var expected = experience();
        var entity =
                new JobExperienceDocument(
                        expected.getId(),
                        expected.getResumeId(),
                        expected.getPosition(),
                        expected.getCompanyName(),
                        expected.getDescription(),
                        expected.getStartDate(),
                        expected.getEndDate());

        var result = JobExperienceMongoMapper.toModel(entity);

        assertEquals(expected.getId(), result.getId());
        assertEquals(expected.getResumeId(), result.getResumeId());
        assertEquals(expected.getPosition(), result.getPosition());
        assertEquals(expected.getCompanyName(), result.getCompanyName());
        assertEquals(expected.getDescription(), result.getDescription());
        assertEquals(expected.getStartDate(), result.getStartDate());
        assertEquals(expected.getEndDate(), result.getEndDate());
    }

    @Test
    @Tag("equivalence")
    void toModel_whenNull_returnsNull() {

        var result = JobExperienceMongoMapper.toModel(null);

        assertNull(result);
    }
}
