package com.ofdun.jobfinder.features.vacancy.domain.service;

import static com.ofdun.jobfinder.support.TestDataMother.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import com.ofdun.jobfinder.features.vacancy.domain.repository.*;
import java.util.*;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class BasicDraftVacancyServiceTest {
    @Mock private VacancyRepository vacancies;
    @Mock private DraftVacancyRepository drafts;
    @InjectMocks private BasicDraftVacancyService service;

    @Test
    @Tag("equivalence")
    void createDraft_whenVacancyExists_savesIndependentSnapshot() {
        var model = vacancy();
        when(vacancies.getVacancyById(1L)).thenReturn(Optional.of(model));
        when(drafts.save(any())).thenAnswer(invocation -> invocation.getArgument(0));

        var result = service.createDraft(1L);

        assertEquals(1L, result.getVacancyId());
        assertNotNull(result.getVersionTimestamp());
        assertTrue(result.getSnapshot().contains("Java developer"));
        assertNull(result.getId());
    }

    @Test
    @Tag("equivalence")
    void createDraft_whenVacancyMissing_throwsNotFound() {
        when(vacancies.getVacancyById(404L)).thenReturn(Optional.empty());

        assertThrows(
                com.ofdun.jobfinder.features.vacancy.exception.VacancyNotFoundException.class,
                () -> service.createDraft(404L));

        verifyNoInteractions(drafts);
    }

    @Test
    @Tag("equivalence")
    void getDrafts_whenVacancyExists_returnsSnapshots() {
        when(vacancies.getVacancyById(2L)).thenReturn(Optional.of(vacancy()));
        var expected = List.of(draft());
        when(drafts.getByVacancyId(2L)).thenReturn(expected);

        var result = service.getDrafts(2L);

        assertEquals(expected, result);
    }

    @Test
    @Tag("equivalence")
    void getDrafts_whenVacancyMissing_throwsNotFound() {
        when(vacancies.getVacancyById(404L)).thenReturn(Optional.empty());

        assertThrows(
                com.ofdun.jobfinder.features.vacancy.exception.VacancyNotFoundException.class,
                () -> service.getDrafts(404L));

        verifyNoInteractions(drafts);
    }

    @Test
    @Tag("equivalence")
    void applyDraft_whenOwned_restoresContentAndActivates() {
        when(drafts.getById(1L)).thenReturn(Optional.of(draft()));
        when(vacancies.updateVacancy(any())).thenAnswer(invocation -> invocation.getArgument(0));

        var result = service.applyDraft(2L, 1L);

        assertEquals(2L, result.getId());
        assertEquals("Java developer", result.getDescription());
        assertEquals(
                com.ofdun.jobfinder.features.vacancy.enums.VacancyStatus.ACTIVE,
                result.getStatus());
    }

    @Test
    @Tag("equivalence")
    void applyDraft_whenOtherOwner_rejectsSnapshot() {
        when(drafts.getById(1L)).thenReturn(Optional.of(draft()));

        assertThrows(IllegalArgumentException.class, () -> service.applyDraft(99L, 1L));

        verifyNoInteractions(vacancies);
    }

    @Test
    @Tag("equivalence")
    void applyDraft_whenMalformedSnapshot_preservesStorage() {
        var model = draft();
        model.setSnapshot("{broken");
        when(drafts.getById(1L)).thenReturn(Optional.of(model));

        var error = assertThrows(IllegalStateException.class, () -> service.applyDraft(2L, 1L));

        assertNotNull(error.getCause());
        verifyNoInteractions(vacancies);
    }

    @Test
    @Tag("equivalence")
    void deleteDraft_whenOwned_deletesSnapshot() {
        when(drafts.getById(1L)).thenReturn(Optional.of(draft()));

        service.deleteDraft(2L, 1L);

        verify(drafts).deleteById(1L);
    }

    @Test
    @Tag("equivalence")
    void deleteDraft_whenMissing_rejectsDeletion() {
        when(drafts.getById(404L)).thenReturn(Optional.empty());

        assertThrows(IllegalArgumentException.class, () -> service.deleteDraft(2L, 404L));

        verify(drafts, never()).deleteById(any());
    }
}
