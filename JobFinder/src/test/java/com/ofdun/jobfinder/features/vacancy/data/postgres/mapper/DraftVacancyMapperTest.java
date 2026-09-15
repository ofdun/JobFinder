package com.ofdun.jobfinder.features.vacancy.data.postgres.mapper;

import static com.ofdun.jobfinder.support.TestDataMother.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import com.ofdun.jobfinder.features.vacancy.data.postgres.entity.DraftVacancyEntity;
import java.util.*;
import org.junit.jupiter.api.*;
import org.mockito.*;

class DraftVacancyMapperTest {

    @Test
    @Tag("equivalence")
    void toEntity_whenPresent_mapsAllFields() {
        var model = draft();

        var result = DraftVacancyMapper.toEntity(model);

        assertEquals(model.getId(), result.getId());
        assertEquals(model.getVacancyId(), result.getVacancyId());
        assertEquals(model.getVersionTimestamp(), result.getVersionTimestamp());
        assertEquals(model.getSnapshot(), result.getSnapshot());
    }

    @Test
    @Tag("equivalence")
    void toEntity_whenNull_returnsNull() {

        var result = DraftVacancyMapper.toEntity(null);

        assertNull(result);
    }

    @Test
    @Tag("equivalence")
    void toModel_whenPresent_mapsAllFields() {
        var expected = draft();
        var entity =
                new DraftVacancyEntity(
                        expected.getId(),
                        expected.getVacancyId(),
                        expected.getVersionTimestamp(),
                        expected.getSnapshot());

        var result = DraftVacancyMapper.toModel(entity);

        assertEquals(expected.getId(), result.getId());
        assertEquals(expected.getVacancyId(), result.getVacancyId());
        assertEquals(expected.getVersionTimestamp(), result.getVersionTimestamp());
        assertEquals(expected.getSnapshot(), result.getSnapshot());
    }

    @Test
    @Tag("equivalence")
    void toModel_whenNull_returnsNull() {

        var result = DraftVacancyMapper.toModel(null);

        assertNull(result);
    }
}
