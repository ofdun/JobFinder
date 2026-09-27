package com.ofdun.jobfinder.features.skill.data.postgres.mapper;

import static com.ofdun.jobfinder.support.TestDataMother.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import com.ofdun.jobfinder.features.skill.data.postgres.entity.SkillEntity;
import java.util.*;
import org.junit.jupiter.api.*;
import org.mockito.*;

class SkillMapperTest {

    @Test
    @Tag("equivalence")
    void toEntity_whenPresent_mapsAllFields() {
        var model = skill();

        var result = SkillMapper.toEntity(model);

        assertEquals(model.getId(), result.getId());
        assertEquals(model.getName(), result.getName());
    }

    @Test
    @Tag("equivalence")
    void toEntity_whenNull_returnsNull() {

        var result = SkillMapper.toEntity(null);

        assertNull(result);
    }

    @Test
    @Tag("equivalence")
    void toModel_whenPresent_mapsAllFields() {
        var expected = skill();
        var entity = new SkillEntity(expected.getId(), expected.getName());

        var result = SkillMapper.toModel(entity);

        assertEquals(expected.getId(), result.getId());
        assertEquals(expected.getName(), result.getName());
    }

    @Test
    @Tag("equivalence")
    void toModel_whenNull_returnsNull() {

        var result = SkillMapper.toModel(null);

        assertNull(result);
    }
}
