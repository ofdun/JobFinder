package com.ofdun.jobfinder.features.vacancy.domain.validator;

import static com.ofdun.jobfinder.support.TestDataMother.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import com.ofdun.jobfinder.features.vacancy.domain.repository.VacancyRepository;
import com.ofdun.jobfinder.features.vacancy.exception.*;
import java.util.*;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class BasicVacancyValidatorTest {
    @Mock private VacancyRepository repository;
    @InjectMocks private BasicVacancyValidator validator;

    @Test
    @Tag("equivalence")
    void validateVacancyForCreate_whenValid_acceptsModel() {
        var model = vacancy();

        assertDoesNotThrow(() -> validator.validateVacancyForCreate(model));

        verify(repository, never()).getVacancyById(any());
    }

    @Test
    @Tag("equivalence")
    void validateVacancyForCreate_whenNull_rejectsModel() {

        var error =
                assertThrows(
                        NullPointerException.class, () -> validator.validateVacancyForCreate(null));

        assertNotNull(error);
        verifyNoInteractions(repository);
    }

    @Test
    @Tag("equivalence")
    void validateVacancyForUpdate_whenPresent_acceptsModel() {
        var model = vacancy();
        when(repository.getVacancyById(1L)).thenReturn(Optional.of(model));

        assertDoesNotThrow(() -> validator.validateVacancyForUpdate(model));

        verify(repository).getVacancyById(1L);
    }

    @Test
    @Tag("equivalence")
    void validateVacancyForUpdate_whenMissing_throwsNotFound() {
        var model = vacancy();
        when(repository.getVacancyById(1L)).thenReturn(Optional.empty());

        var error =
                assertThrows(
                        VacancyNotFoundException.class,
                        () -> validator.validateVacancyForUpdate(model));

        assertNotNull(error);
    }

    @org.junit.jupiter.params.ParameterizedTest
    @Tag("boundary")
    @Tag("equivalence")
    @org.junit.jupiter.params.provider.NullSource
    @org.junit.jupiter.params.provider.ValueSource(longs = {-1, 0})
    void validateVacancyForUpdate_invalidIdSkipsStorage(Long id) {
        var model = vacancy();
        model.setId(id);

        assertThrows(
                IllegalArgumentException.class, () -> validator.validateVacancyForUpdate(model));

        verifyNoInteractions(repository);
    }

    @Test
    @Tag("equivalence")
    void validateVacancyForDelete_whenPresent_acceptsModel() {
        var model = vacancy();
        when(repository.getVacancyById(1L)).thenReturn(Optional.of(model));

        assertDoesNotThrow(() -> validator.validateVacancyForDelete(model.getId()));

        verify(repository).getVacancyById(1L);
    }

    @Test
    @Tag("equivalence")
    void validateVacancyForDelete_whenMissing_throwsNotFound() {
        var model = vacancy();
        when(repository.getVacancyById(1L)).thenReturn(Optional.empty());

        var error =
                assertThrows(
                        VacancyNotFoundException.class,
                        () -> validator.validateVacancyForDelete(model.getId()));

        assertNotNull(error);
    }

    @org.junit.jupiter.params.ParameterizedTest
    @Tag("boundary")
    @Tag("equivalence")
    @org.junit.jupiter.params.provider.NullSource
    @org.junit.jupiter.params.provider.ValueSource(longs = {-1, 0})
    void validateVacancyForDelete_invalidIdSkipsStorage(Long id) {
        var model = vacancy();
        model.setId(id);

        assertThrows(
                IllegalArgumentException.class,
                () -> validator.validateVacancyForDelete(model.getId()));

        verifyNoInteractions(repository);
    }
}
