package com.ofdun.jobfinder.features.resume.data.postgres.mapper;

import static com.ofdun.jobfinder.support.TestDataMother.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import com.ofdun.jobfinder.features.resume.data.postgres.entity.ResumeEntity;
import java.util.*;
import org.junit.jupiter.api.*;
import org.mockito.*;

class ResumeMapperTest {

    @Test
    @Tag("equivalence")
    void toEntity_whenPresent_mapsAllFields() {
        var model = resume();

        var result = ResumeMapper.toEntity(model);

        assertEquals(model.getId(), result.getId());
        assertEquals(model.getApplicantId(), result.getApplicantId());
        assertEquals(model.getCategoryId(), result.getCategoryId());
        assertEquals(model.getDescription(), result.getDescription());
        assertEquals(model.getSkillIds(), result.getSkillIds());
        assertEquals(model.getLanguageIds(), result.getLanguages());
        assertEquals(model.getDate(), result.getCreationDate());
    }

    @Test
    @Tag("equivalence")
    void toEntity_whenNull_returnsNull() {

        var result = ResumeMapper.toEntity(null);

        assertNull(result);
    }

    @Test
    @Tag("equivalence")
    void toModel_whenPresent_mapsAllFields() {
        var expected = resume();
        var entity =
                new ResumeEntity(
                        expected.getId(),
                        expected.getApplicantId(),
                        expected.getCategoryId(),
                        expected.getDescription(),
                        expected.getSkillIds(),
                        expected.getLanguageIds(),
                        expected.getDate());

        var result = ResumeMapper.toModel(entity);

        assertEquals(expected.getId(), result.getId());
        assertEquals(expected.getApplicantId(), result.getApplicantId());
        assertEquals(expected.getCategoryId(), result.getCategoryId());
        assertEquals(expected.getDescription(), result.getDescription());
        assertEquals(expected.getSkillIds(), result.getSkillIds());
        assertEquals(expected.getLanguageIds(), result.getLanguageIds());
        assertEquals(expected.getDate(), result.getDate());
    }

    @Test
    @Tag("equivalence")
    void toModel_whenNull_returnsNull() {

        var result = ResumeMapper.toModel(null);

        assertNull(result);
    }
}
