package com.ofdun.jobfinder.features.vacancy.data.mongo.mapper;

import static com.ofdun.jobfinder.support.TestDataMother.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import com.ofdun.jobfinder.features.vacancy.data.mongo.entity.VacancyDocument;
import java.util.*;
import org.junit.jupiter.api.*;
import org.mockito.*;

class VacancyMongoMapperTest {

    @Test
    @Tag("equivalence")
    void toEntity_whenPresent_mapsAllFields() {
        var model = vacancy();

        var result = VacancyMongoMapper.toEntity(model);

        assertEquals(model.getId(), result.getId());
        assertEquals(model.getEmployerId(), result.getEmployerId());
        assertEquals(model.getLocationId(), result.getLocationId());
        assertEquals(model.getSalary(), result.getSalary());
        assertEquals(model.getSkillIds(), result.getSkillIds());
        assertEquals(model.getLanguageIds(), result.getLanguageIds());
        assertEquals(model.getPaymentFrequency(), result.getPaymentFrequency());
        assertEquals(model.getExperience(), result.getWorkExperience());
        assertEquals(model.getJobFormat(), result.getWorkFormat());
        assertEquals(model.getEmploymentType(), result.getEmploymentType());
        assertEquals(model.getDescription(), result.getDescription());
        assertEquals(model.getPublicationDate(), result.getPublicationDate());
        assertEquals(model.getAddress(), result.getAddress());
        assertEquals(model.getStatus(), result.getStatus());
    }

    @Test
    @Tag("equivalence")
    void toEntity_whenNull_returnsNull() {

        var result = VacancyMongoMapper.toEntity(null);

        assertNull(result);
    }

    @Test
    @Tag("equivalence")
    void toModel_whenPresent_mapsAllFields() {
        var expected = vacancy();
        var entity =
                new VacancyDocument(
                        expected.getId(),
                        expected.getEmployerId(),
                        expected.getLocationId(),
                        expected.getSalary(),
                        expected.getSkillIds(),
                        expected.getLanguageIds(),
                        expected.getPaymentFrequency(),
                        expected.getExperience(),
                        expected.getJobFormat(),
                        expected.getEmploymentType(),
                        expected.getDescription(),
                        expected.getPublicationDate(),
                        expected.getAddress(),
                        expected.getStatus());

        var result = VacancyMongoMapper.toModel(entity);

        assertEquals(expected.getId(), result.getId());
        assertEquals(expected.getEmployerId(), result.getEmployerId());
        assertEquals(expected.getLocationId(), result.getLocationId());
        assertEquals(expected.getSalary(), result.getSalary());
        assertEquals(expected.getSkillIds(), result.getSkillIds());
        assertEquals(expected.getLanguageIds(), result.getLanguageIds());
        assertEquals(expected.getPaymentFrequency(), result.getPaymentFrequency());
        assertEquals(expected.getExperience(), result.getExperience());
        assertEquals(expected.getJobFormat(), result.getJobFormat());
        assertEquals(expected.getEmploymentType(), result.getEmploymentType());
        assertEquals(expected.getDescription(), result.getDescription());
        assertEquals(expected.getPublicationDate(), result.getPublicationDate());
        assertEquals(expected.getAddress(), result.getAddress());
        assertEquals(expected.getStatus(), result.getStatus());
    }

    @Test
    @Tag("equivalence")
    void toModel_whenNull_returnsNull() {

        var result = VacancyMongoMapper.toModel(null);

        assertNull(result);
    }
}
