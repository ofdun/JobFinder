package com.ofdun.jobfinder.features.application.data.mongo.mapper;

import static com.ofdun.jobfinder.support.TestDataMother.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import com.ofdun.jobfinder.features.application.data.mongo.entity.ApplicationDocument;
import java.util.*;
import org.junit.jupiter.api.*;
import org.mockito.*;

class ApplicationMongoMapperTest {

    @Test
    @Tag("equivalence")
    void toEntity_whenPresent_mapsAllFields() {
        var model = application();

        var result = ApplicationMongoMapper.toEntity(model);

        assertEquals(model.getId(), result.getId());
        assertEquals(model.getVacancyId(), result.getVacancyId());
        assertEquals(model.getResumeId(), result.getResumeId());
        assertEquals(model.getApplicationDate(), result.getApplicationDate());
        assertEquals(model.getApplicationStatus(), result.getApplicationStatus());
    }

    @Test
    @Tag("equivalence")
    void toEntity_whenNull_returnsNull() {

        var result = ApplicationMongoMapper.toEntity(null);

        assertNull(result);
    }

    @Test
    @Tag("equivalence")
    void toModel_whenPresent_mapsAllFields() {
        var expected = application();
        var entity =
                new ApplicationDocument(
                        expected.getId(),
                        expected.getVacancyId(),
                        expected.getResumeId(),
                        expected.getApplicationDate(),
                        expected.getApplicationStatus());

        var result = ApplicationMongoMapper.toModel(entity);

        assertEquals(expected.getId(), result.getId());
        assertEquals(expected.getVacancyId(), result.getVacancyId());
        assertEquals(expected.getResumeId(), result.getResumeId());
        assertEquals(expected.getApplicationDate(), result.getApplicationDate());
        assertEquals(expected.getApplicationStatus(), result.getApplicationStatus());
    }

    @Test
    @Tag("equivalence")
    void toModel_whenNull_returnsNull() {

        var result = ApplicationMongoMapper.toModel(null);

        assertNull(result);
    }
}
