package com.ofdun.jobfinder.features.language.data.postgres.mapper;

import static com.ofdun.jobfinder.support.TestDataMother.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import com.ofdun.jobfinder.features.language.data.postgres.entity.LanguageEntity;
import java.util.*;
import org.junit.jupiter.api.*;
import org.mockito.*;

class LanguageMapperTest {

    @Test
    @Tag("equivalence")
    void toEntity_whenPresent_mapsAllFields() {
        var model = language();

        var result = LanguageMapper.toEntity(model);

        assertEquals(model.getId(), result.getId());
        assertEquals(model.getName(), result.getName());
        assertEquals(model.getProficiencyLevel(), result.getProficiencyLevel());
    }

    @Test
    @Tag("equivalence")
    void toEntity_whenNull_returnsNull() {

        var result = LanguageMapper.toEntity(null);

        assertNull(result);
    }

    @Test
    @Tag("equivalence")
    void toModel_whenPresent_mapsAllFields() {
        var expected = language();
        var entity =
                new LanguageEntity(
                        expected.getId(), expected.getName(), expected.getProficiencyLevel());

        var result = LanguageMapper.toModel(entity);

        assertEquals(expected.getId(), result.getId());
        assertEquals(expected.getName(), result.getName());
        assertEquals(expected.getProficiencyLevel(), result.getProficiencyLevel());
    }

    @Test
    @Tag("equivalence")
    void toModel_whenNull_returnsNull() {

        var result = LanguageMapper.toModel(null);

        assertNull(result);
    }
}
