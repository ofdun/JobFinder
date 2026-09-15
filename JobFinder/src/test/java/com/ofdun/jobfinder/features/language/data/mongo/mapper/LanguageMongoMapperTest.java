package com.ofdun.jobfinder.features.language.data.mongo.mapper;

import static com.ofdun.jobfinder.support.TestDataMother.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import com.ofdun.jobfinder.features.language.data.mongo.entity.LanguageDocument;
import java.util.*;
import org.junit.jupiter.api.*;
import org.mockito.*;

class LanguageMongoMapperTest {

    @Test
    @Tag("equivalence")
    void toEntity_whenPresent_mapsAllFields() {
        var model = language();

        var result = LanguageMongoMapper.toEntity(model);

        assertEquals(model.getId(), result.getId());
        assertEquals(model.getName(), result.getName());
        assertEquals(model.getProficiencyLevel(), result.getProficiencyLevel());
    }

    @Test
    @Tag("equivalence")
    void toEntity_whenNull_returnsNull() {

        var result = LanguageMongoMapper.toEntity(null);

        assertNull(result);
    }

    @Test
    @Tag("equivalence")
    void toModel_whenPresent_mapsAllFields() {
        var expected = language();
        var entity =
                new LanguageDocument(
                        expected.getId(), expected.getName(), expected.getProficiencyLevel());

        var result = LanguageMongoMapper.toModel(entity);

        assertEquals(expected.getId(), result.getId());
        assertEquals(expected.getName(), result.getName());
        assertEquals(expected.getProficiencyLevel(), result.getProficiencyLevel());
    }

    @Test
    @Tag("equivalence")
    void toModel_whenNull_returnsNull() {

        var result = LanguageMongoMapper.toModel(null);

        assertNull(result);
    }
}
