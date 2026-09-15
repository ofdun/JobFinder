package com.ofdun.jobfinder.features.skill.data.mongo.mapper;

import static com.ofdun.jobfinder.support.TestDataMother.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import com.ofdun.jobfinder.features.skill.data.mongo.entity.SkillDocument;
import java.util.*;
import org.junit.jupiter.api.*;
import org.mockito.*;

class SkillMongoMapperTest {

    @Test
    @Tag("equivalence")
    void toEntity_whenPresent_mapsAllFields() {
        var model = skill();

        var result = SkillMongoMapper.toEntity(model);

        assertEquals(model.getId(), result.getId());
        assertEquals(model.getName(), result.getName());
    }

    @Test
    @Tag("equivalence")
    void toEntity_whenNull_returnsNull() {

        var result = SkillMongoMapper.toEntity(null);

        assertNull(result);
    }

    @Test
    @Tag("equivalence")
    void toModel_whenPresent_mapsAllFields() {
        var expected = skill();
        var entity = new SkillDocument(expected.getId(), expected.getName());

        var result = SkillMongoMapper.toModel(entity);

        assertEquals(expected.getId(), result.getId());
        assertEquals(expected.getName(), result.getName());
    }

    @Test
    @Tag("equivalence")
    void toModel_whenNull_returnsNull() {

        var result = SkillMongoMapper.toModel(null);

        assertNull(result);
    }
}
