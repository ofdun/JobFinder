package com.ofdun.jobfinder.features.applicant.domain.validator;

import static com.ofdun.jobfinder.support.TestDataMother.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import com.ofdun.jobfinder.features.applicant.domain.repository.ApplicantRepository;
import com.ofdun.jobfinder.features.applicant.exception.*;
import java.util.*;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class BasicApplicantValidatorTest {
    @Mock private ApplicantRepository repository;
    @InjectMocks private BasicApplicantValidator validator;

    @Test
    @Tag("equivalence")
    void validateApplicantForCreate_whenValid_acceptsModel() {
        var model = applicant();
        when(repository.getApplicantByEmail(model.getEmail())).thenReturn(Optional.empty());

        assertDoesNotThrow(() -> validator.validateApplicantForCreate(model));

        verify(repository, never()).getApplicantById(any());
    }

    @Test
    @Tag("equivalence")
    void validateApplicantForCreate_whenDuplicate_rejectsModel() {
        var model = applicant();
        when(repository.getApplicantByEmail(model.getEmail())).thenReturn(Optional.of(model));

        var error =
                assertThrows(
                        ApplicantAlreadyExistsException.class,
                        () -> validator.validateApplicantForCreate(model));

        assertNotNull(error);
    }

    @Test
    @Tag("equivalence")
    void validateApplicantForUpdate_whenPresent_acceptsModel() {
        var model = applicant();
        when(repository.getApplicantById(1L)).thenReturn(Optional.of(model));

        assertDoesNotThrow(() -> validator.validateApplicantForUpdate(model));

        verify(repository).getApplicantById(1L);
    }

    @Test
    @Tag("equivalence")
    void validateApplicantForUpdate_whenMissing_throwsNotFound() {
        var model = applicant();
        when(repository.getApplicantById(1L)).thenReturn(Optional.empty());

        var error =
                assertThrows(
                        ApplicantNotFoundException.class,
                        () -> validator.validateApplicantForUpdate(model));

        assertNotNull(error);
    }

    @org.junit.jupiter.params.ParameterizedTest
    @Tag("boundary")
    @Tag("equivalence")
    @org.junit.jupiter.params.provider.NullSource
    @org.junit.jupiter.params.provider.ValueSource(longs = {-1, 0})
    void validateApplicantForUpdate_invalidIdSkipsStorage(Long id) {
        var model = new com.ofdun.jobfinder.support.ApplicantBuilder().withId(id).build();

        assertThrows(
                IllegalArgumentException.class, () -> validator.validateApplicantForUpdate(model));

        verifyNoInteractions(repository);
    }

    @Test
    @Tag("equivalence")
    void validateApplicantForDelete_whenPresent_acceptsModel() {
        var model = applicant();
        when(repository.getApplicantById(1L)).thenReturn(Optional.of(model));

        assertDoesNotThrow(() -> validator.validateApplicantForDelete(model.getId()));

        verify(repository).getApplicantById(1L);
    }

    @Test
    @Tag("equivalence")
    void validateApplicantForDelete_whenMissing_throwsNotFound() {
        var model = applicant();
        when(repository.getApplicantById(1L)).thenReturn(Optional.empty());

        var error =
                assertThrows(
                        ApplicantNotFoundException.class,
                        () -> validator.validateApplicantForDelete(model.getId()));

        assertNotNull(error);
    }

    @org.junit.jupiter.params.ParameterizedTest
    @Tag("boundary")
    @Tag("equivalence")
    @org.junit.jupiter.params.provider.NullSource
    @org.junit.jupiter.params.provider.ValueSource(longs = {-1, 0})
    void validateApplicantForDelete_invalidIdSkipsStorage(Long id) {
        var model = new com.ofdun.jobfinder.support.ApplicantBuilder().withId(id).build();

        assertThrows(
                IllegalArgumentException.class,
                () -> validator.validateApplicantForDelete(model.getId()));

        verifyNoInteractions(repository);
    }
}
