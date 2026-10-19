package com.ofdun.jobfinder.features.api;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import com.ofdun.jobfinder.features.application.domain.service.ApplicationService;
import com.ofdun.jobfinder.features.auth.enums.AccountType;
import com.ofdun.jobfinder.features.auth.security.JobFinderPrincipal;
import com.ofdun.jobfinder.features.resume.domain.repository.RelationalResumeRepository;
import com.ofdun.jobfinder.features.vacancy.domain.service.VacancyService;
import com.ofdun.jobfinder.support.TestDataMother;
import java.util.*;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.*;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;

class ApiAccessTest {
    private final VacancyService vacancies = mock(VacancyService.class);
    private final RelationalResumeRepository resumes = mock(RelationalResumeRepository.class);
    private final ApplicationService applications = mock(ApplicationService.class);
    private final ApiAccess access = new ApiAccess(vacancies, resumes, applications);

    private Authentication actor(AccountType role, long id) {
        return new UsernamePasswordAuthenticationToken(
                new JobFinderPrincipal(id, role), null, List.of());
    }

    static java.util.stream.Stream<Authentication> invalidAuthentication() {
        return java.util.stream.Stream.of(
                null,
                new UsernamePasswordAuthenticationToken(
                        new JobFinderPrincipal(1L, AccountType.APPLICANT), null),
                new UsernamePasswordAuthenticationToken("unrecognized", null, List.of()));
    }

    @ParameterizedTest
    @MethodSource("invalidAuthentication")
    void unauthenticatedApplicationReadNeverReachesStorage(Authentication auth) {
        var error = assertThrows(ApiFailure.class, () -> access.application(auth, 1L));
        assertEquals(401, error.getStatus());
        verifyNoInteractions(applications, vacancies, resumes);
    }

    @ParameterizedTest
    @CsvSource({"APPLICANT,2,true", "APPLICANT,3,false", "EMPLOYER,3,true"})
    void resumeReadUsesOwnerForApplicantsAndAllowsEmployers(
            AccountType role, long id, boolean allowed) {
        var model = TestDataMother.resume();
        when(resumes.getResumeById(1L)).thenReturn(Optional.of(model));
        if (allowed) assertSame(model, access.readResume(actor(role, id), 1L));
        else
            assertEquals(
                    403,
                    assertThrows(ApiFailure.class, () -> access.readResume(actor(role, id), 1L))
                            .getStatus());
    }

    @ParameterizedTest
    @EnumSource(AccountType.class)
    void missingResumeReturnsNotFoundForAuthenticatedReaders(AccountType role) {
        when(resumes.getResumeById(1L)).thenReturn(Optional.empty());
        assertEquals(
                404,
                assertThrows(ApiFailure.class, () -> access.readResume(actor(role, 1), 1L))
                        .getStatus());
    }

    @ParameterizedTest
    @CsvSource({"APPLICANT,2,true", "APPLICANT,9,false", "EMPLOYER,2,true", "EMPLOYER,9,false"})
    void applicationReadChecksOwnershipThroughCorrectResource(
            AccountType role, long actorId, boolean allowed) {
        var application = TestDataMother.application();
        when(applications.getApplication(1L)).thenReturn(application);
        if (role == AccountType.APPLICANT)
            when(resumes.getResumeById(3L)).thenReturn(Optional.of(TestDataMother.resume()));
        else when(vacancies.getVacancyById(2L)).thenReturn(TestDataMother.vacancy());
        if (allowed) assertSame(application, access.application(actor(role, actorId), 1L));
        else
            assertEquals(
                    403,
                    assertThrows(
                                    ApiFailure.class,
                                    () -> access.application(actor(role, actorId), 1L))
                            .getStatus());
        if (role == AccountType.APPLICANT) verifyNoInteractions(vacancies);
        else verifyNoInteractions(resumes);
    }

    @Test
    void duplicateApplicationCannotReachCreation() {
        when(resumes.getResumeById(3L)).thenReturn(Optional.of(TestDataMother.resume()));
        when(vacancies.getVacancyById(2L)).thenReturn(TestDataMother.vacancy());
        when(applications.getApplicationsByVacancyId(2L))
                .thenReturn(List.of(TestDataMother.application()));
        assertEquals(
                409,
                assertThrows(
                                ApiFailure.class,
                                () ->
                                        access.newApplication(
                                                actor(AccountType.APPLICANT, 2), 2L, 3L))
                        .getStatus());
        verify(applications, never()).saveApplication(any());
    }

    @ParameterizedTest
    @NullSource
    @ValueSource(strings = {"0", "-1", "9223372036854775808", "1.5", "abc"})
    void malformedResourceIdsCannotBeUsed(String value) {
        assertThrows(IllegalArgumentException.class, () -> ApiAccess.id(value));
    }

    @ParameterizedTest
    @ValueSource(strings = {"1", "9223372036854775807"})
    void resourceIdsSupportFullPositiveLongRange(String value) {
        assertEquals(Long.parseLong(value), ApiAccess.id(value));
    }
}
