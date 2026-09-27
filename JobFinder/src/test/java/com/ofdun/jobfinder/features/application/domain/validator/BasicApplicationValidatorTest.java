package com.ofdun.jobfinder.features.application.domain.validator;

import static com.ofdun.jobfinder.support.TestDataMother.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import com.ofdun.jobfinder.features.application.domain.repository.ApplicationRepository;
import com.ofdun.jobfinder.features.application.exception.*;
import java.util.*;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class BasicApplicationValidatorTest {
    @Mock private ApplicationRepository repository;
    @InjectMocks private BasicApplicationValidator validator;

    @Test
    @Tag("equivalence")
    void validateApplicationForCreate_whenValid_acceptsModel() {
        var model = application();

        assertDoesNotThrow(() -> validator.validateApplicationForCreate(model));

        verify(repository, never()).getApplicationById(any());
    }

    @Test
    @Tag("equivalence")
    void validateApplicationForCreate_whenNull_rejectsModel() {

        var error =
                assertThrows(
                        NullPointerException.class,
                        () -> validator.validateApplicationForCreate(null));

        assertNotNull(error);
        verifyNoInteractions(repository);
    }

    @Test
    @Tag("equivalence")
    void validateApplicationForUpdate_whenPresent_acceptsModel() {
        var model = application();
        when(repository.getApplicationById(1L)).thenReturn(Optional.of(model));

        assertDoesNotThrow(() -> validator.validateApplicationForUpdate(model));

        verify(repository).getApplicationById(1L);
    }

    @Test
    @Tag("equivalence")
    void validateApplicationForUpdate_whenMissing_throwsNotFound() {
        var model = application();
        when(repository.getApplicationById(1L)).thenReturn(Optional.empty());

        var error =
                assertThrows(
                        ApplicationNotFoundException.class,
                        () -> validator.validateApplicationForUpdate(model));

        assertNotNull(error);
    }

    @org.junit.jupiter.params.ParameterizedTest
    @Tag("boundary")
    @Tag("equivalence")
    @org.junit.jupiter.params.provider.NullSource
    @org.junit.jupiter.params.provider.ValueSource(longs = {-1, 0})
    void validateApplicationForUpdate_invalidIdSkipsStorage(Long id) {
        var model = application();
        model.setId(id);

        assertThrows(
                IllegalArgumentException.class,
                () -> validator.validateApplicationForUpdate(model));

        verifyNoInteractions(repository);
    }

    @Test
    @Tag("equivalence")
    void validateApplicationForDelete_whenPresent_acceptsModel() {
        var model = application();
        when(repository.getApplicationById(1L)).thenReturn(Optional.of(model));

        assertDoesNotThrow(() -> validator.validateApplicationForDelete(model.getId()));

        verify(repository).getApplicationById(1L);
    }

    @Test
    @Tag("equivalence")
    void validateApplicationForDelete_whenMissing_throwsNotFound() {
        var model = application();
        when(repository.getApplicationById(1L)).thenReturn(Optional.empty());

        var error =
                assertThrows(
                        ApplicationNotFoundException.class,
                        () -> validator.validateApplicationForDelete(model.getId()));

        assertNotNull(error);
    }

    @org.junit.jupiter.params.ParameterizedTest
    @Tag("boundary")
    @Tag("equivalence")
    @org.junit.jupiter.params.provider.NullSource
    @org.junit.jupiter.params.provider.ValueSource(longs = {-1, 0})
    void validateApplicationForDelete_invalidIdSkipsStorage(Long id) {
        var model = application();
        model.setId(id);

        assertThrows(
                IllegalArgumentException.class,
                () -> validator.validateApplicationForDelete(model.getId()));

        verifyNoInteractions(repository);
    }

    @org.junit.jupiter.params.ParameterizedTest
    @Tag("combinatorial")
    @org.junit.jupiter.params.provider.CsvSource(
            value = {
                "1,1,true",
                "1,0,false",
                "1,null,false",
                "1,-1,false",
                "0,1,false",
                "0,0,false",
                "0,null,false",
                "0,-1,false",
                "null,1,false",
                "null,0,false",
                "null,null,false",
                "null,-1,false",
                "-1,1,false",
                "-1,0,false",
                "-1,null,false",
                "-1,-1,false"
            },
            nullValues = "null")
    void validateApplicationForCreate_checksBothForeignKeys(
            Long resumeId, Long vacancyId, boolean valid) {
        var model = application();
        model.setResumeId(resumeId);
        model.setVacancyId(vacancyId);

        if (valid) {
            assertDoesNotThrow(() -> validator.validateApplicationForCreate(model));
        } else {
            assertThrows(
                    IllegalArgumentException.class,
                    () -> validator.validateApplicationForCreate(model));
        }

        verifyNoInteractions(repository);
    }
}
