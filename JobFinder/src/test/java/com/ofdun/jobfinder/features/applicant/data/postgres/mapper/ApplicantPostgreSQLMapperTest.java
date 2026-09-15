package com.ofdun.jobfinder.features.applicant.data.postgres.mapper;

import static com.ofdun.jobfinder.support.TestDataMother.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import com.ofdun.jobfinder.features.applicant.data.postgres.entity.ApplicantEntity;
import java.util.*;
import org.junit.jupiter.api.*;
import org.mockito.*;

class ApplicantPostgreSQLMapperTest {

    @Test
    @Tag("equivalence")
    void toEntity_whenPresent_mapsAllFields() {
        var model = applicant();

        var result = ApplicantPostgreSQLMapper.toEntity(model);

        assertEquals(model.getId(), result.getId());
        assertEquals(model.getName(), result.getName());
        assertEquals(model.getEmail(), result.getEmail());
        assertEquals(model.getPasswordHash(), result.getPasswordHash());
        assertEquals(model.getAddress(), result.getAddress());
        assertEquals(model.getPhoneNumber(), result.getPhoneNumber());
        assertEquals(model.getLocationId(), result.getLocationId());
    }

    @Test
    @Tag("equivalence")
    void toEntity_whenNull_returnsNull() {

        var result = ApplicantPostgreSQLMapper.toEntity(null);

        assertNull(result);
    }

    @Test
    @Tag("equivalence")
    void toModel_whenPresent_mapsAllFields() {
        var expected = applicant();
        var entity =
                new ApplicantEntity(
                        expected.getId(),
                        expected.getName(),
                        expected.getEmail(),
                        expected.getPasswordHash(),
                        expected.getAddress(),
                        expected.getPhoneNumber(),
                        expected.getLocationId());

        var result = ApplicantPostgreSQLMapper.toModel(entity);

        assertEquals(expected.getId(), result.getId());
        assertEquals(expected.getName(), result.getName());
        assertEquals(expected.getEmail(), result.getEmail());
        assertEquals(expected.getPasswordHash(), result.getPasswordHash());
        assertEquals(expected.getAddress(), result.getAddress());
        assertEquals(expected.getPhoneNumber(), result.getPhoneNumber());
        assertEquals(expected.getLocationId(), result.getLocationId());
    }

    @Test
    @Tag("equivalence")
    void toModel_whenNull_returnsNull() {

        var result = ApplicantPostgreSQLMapper.toModel(null);

        assertNull(result);
    }
}
