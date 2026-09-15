package com.ofdun.jobfinder.features.employer.domain.validator;

import static com.ofdun.jobfinder.support.TestDataMother.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import com.ofdun.jobfinder.features.employer.domain.repository.EmployerRepository;
import com.ofdun.jobfinder.features.employer.exception.*;
import java.util.*;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class BasicEmployerValidatorTest {
    @Mock private EmployerRepository repository;
    @InjectMocks private BasicEmployerValidator validator;

    @Test
    @Tag("equivalence")
    void validateEmployerForCreate_whenValid_acceptsModel() {
        var model = employer();
        when(repository.getEmployerByEmail(model.getEmail())).thenReturn(Optional.empty());

        assertDoesNotThrow(() -> validator.validateEmployerForCreate(model));

        verify(repository, never()).getEmployerById(any());
    }

    @Test
    @Tag("equivalence")
    void validateEmployerForCreate_whenDuplicate_rejectsModel() {
        var model = employer();
        when(repository.getEmployerByEmail(model.getEmail())).thenReturn(Optional.of(model));

        var error =
                assertThrows(
                        EmployerAlreadyExistsException.class,
                        () -> validator.validateEmployerForCreate(model));

        assertNotNull(error);
    }

    @Test
    @Tag("equivalence")
    void validateEmployerForUpdate_whenPresent_acceptsModel() {
        var model = employer();
        when(repository.getEmployerById(1L)).thenReturn(Optional.of(model));

        assertDoesNotThrow(() -> validator.validateEmployerForUpdate(model));

        verify(repository).getEmployerById(1L);
    }

    @Test
    @Tag("equivalence")
    void validateEmployerForUpdate_whenMissing_throwsNotFound() {
        var model = employer();
        when(repository.getEmployerById(1L)).thenReturn(Optional.empty());

        var error =
                assertThrows(
                        EmployerNotFoundException.class,
                        () -> validator.validateEmployerForUpdate(model));

        assertNotNull(error);
    }

    @org.junit.jupiter.params.ParameterizedTest
    @Tag("boundary")
    @Tag("equivalence")
    @org.junit.jupiter.params.provider.NullSource
    @org.junit.jupiter.params.provider.ValueSource(longs = {-1, 0})
    void validateEmployerForUpdate_invalidIdSkipsStorage(Long id) {
        var model = employer();
        model.setId(id);

        assertThrows(
                IllegalArgumentException.class, () -> validator.validateEmployerForUpdate(model));

        verifyNoInteractions(repository);
    }

    @Test
    @Tag("equivalence")
    void validateEmployerForDelete_whenPresent_acceptsModel() {
        var model = employer();
        when(repository.getEmployerById(1L)).thenReturn(Optional.of(model));

        assertDoesNotThrow(() -> validator.validateEmployerForDelete(model.getId()));

        verify(repository).getEmployerById(1L);
    }

    @Test
    @Tag("equivalence")
    void validateEmployerForDelete_whenMissing_throwsNotFound() {
        var model = employer();
        when(repository.getEmployerById(1L)).thenReturn(Optional.empty());

        var error =
                assertThrows(
                        EmployerNotFoundException.class,
                        () -> validator.validateEmployerForDelete(model.getId()));

        assertNotNull(error);
    }

    @org.junit.jupiter.params.ParameterizedTest
    @Tag("boundary")
    @Tag("equivalence")
    @org.junit.jupiter.params.provider.NullSource
    @org.junit.jupiter.params.provider.ValueSource(longs = {-1, 0})
    void validateEmployerForDelete_invalidIdSkipsStorage(Long id) {
        var model = employer();
        model.setId(id);

        assertThrows(
                IllegalArgumentException.class,
                () -> validator.validateEmployerForDelete(model.getId()));

        verifyNoInteractions(repository);
    }
}
