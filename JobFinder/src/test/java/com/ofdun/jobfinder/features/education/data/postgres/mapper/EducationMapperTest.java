package com.ofdun.jobfinder.features.education.data.postgres.mapper;

import static com.ofdun.jobfinder.support.TestDataMother.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import com.ofdun.jobfinder.features.education.data.postgres.entity.EducationEntity;
import java.util.*;
import org.junit.jupiter.api.*;
import org.mockito.*;

class EducationMapperTest {

    @Test
    @Tag("equivalence")
    void toEntity_whenPresent_mapsAllFields() {
        var model = education();

        var result = EducationMapper.toEntity(model);

        assertEquals(model.getId(), result.getId());
        assertEquals(model.getResumeId(), result.getResumeId());
        assertEquals(model.getEducationDegree(), result.getDegree());
        assertEquals(model.getInstitutionName(), result.getInstitution());
        assertEquals(model.getFaculty(), result.getFaculty());
        assertEquals(model.getDepartment(), result.getDepartment());
        assertEquals(model.getYearOfGraduation(), result.getGraduationYear());
    }

    @Test
    @Tag("equivalence")
    void toEntity_whenNull_returnsNull() {

        var result = EducationMapper.toEntity(null);

        assertNull(result);
    }

    @Test
    @Tag("equivalence")
    void toModel_whenPresent_mapsAllFields() {
        var expected = education();
        var entity =
                new EducationEntity(
                        expected.getId(),
                        expected.getResumeId(),
                        expected.getEducationDegree(),
                        expected.getInstitutionName(),
                        expected.getFaculty(),
                        expected.getDepartment(),
                        expected.getYearOfGraduation());

        var result = EducationMapper.toModel(entity);

        assertEquals(expected.getId(), result.getId());
        assertEquals(expected.getResumeId(), result.getResumeId());
        assertEquals(expected.getEducationDegree(), result.getEducationDegree());
        assertEquals(expected.getInstitutionName(), result.getInstitutionName());
        assertEquals(expected.getFaculty(), result.getFaculty());
        assertEquals(expected.getDepartment(), result.getDepartment());
        assertEquals(expected.getYearOfGraduation(), result.getYearOfGraduation());
    }

    @Test
    @Tag("equivalence")
    void toModel_whenNull_returnsNull() {

        var result = EducationMapper.toModel(null);

        assertNull(result);
    }
}
