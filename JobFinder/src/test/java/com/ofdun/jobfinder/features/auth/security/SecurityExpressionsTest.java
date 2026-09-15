package com.ofdun.jobfinder.features.auth.security;

import static com.ofdun.jobfinder.support.TestDataMother.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import com.ofdun.jobfinder.features.application.domain.repository.ApplicationRepository;
import com.ofdun.jobfinder.features.auth.enums.AccountType;
import com.ofdun.jobfinder.features.resume.domain.repository.RelationalResumeRepository;
import com.ofdun.jobfinder.features.vacancy.domain.repository.VacancyRepository;
import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.EnumSource;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;

@Tag("equivalence")
@ExtendWith(MockitoExtension.class)
class SecurityExpressionsTest {
    @Mock private VacancyRepository vacancies;
    @Mock private RelationalResumeRepository resumes;
    @Mock private ApplicationRepository applications;
    @InjectMocks private SecurityExpressions security;

    private static Authentication authenticated(Long id, AccountType role) {
        return new UsernamePasswordAuthenticationToken(
                new JobFinderPrincipal(id, role), null, List.of());
    }

    private static Stream<Authentication> invalidAuthentications() {
        return Stream.of(
                null,
                new UsernamePasswordAuthenticationToken(
                        new JobFinderPrincipal(2L, AccountType.APPLICANT), null),
                new UsernamePasswordAuthenticationToken("unexpected-principal", null, List.of()));
    }

    @ParameterizedTest
    @CsvSource(
            value = {"2,2,true", "2,3,false", "null,2,false", "2,null,false"},
            nullValues = "null")
    void isSelf_checksPrincipalId(Long principalId, Long requestedId, boolean expected) {
        var authentication = authenticated(principalId, AccountType.APPLICANT);

        var result = security.isSelf(authentication, requestedId);

        assertEquals(expected, result);
    }

    @ParameterizedTest
    @MethodSource("invalidAuthentications")
    void isSelf_whenAuthenticationInvalid_deniesAccess(Authentication authentication) {
        var result = security.isSelf(authentication, 2L);

        assertFalse(result);
    }

    @ParameterizedTest
    @CsvSource(
            value = {"APPLICANT,true", "EMPLOYER,false", "null,false"},
            nullValues = "null")
    void isApplicant_checksAccountType(AccountType role, boolean expected) {
        var authentication = authenticated(2L, role);

        var result = security.isApplicant(authentication);

        assertEquals(expected, result);
    }

    @ParameterizedTest
    @MethodSource("invalidAuthentications")
    void isApplicant_whenAuthenticationInvalid_returnsFalse(Authentication authentication) {
        var result = security.isApplicant(authentication);

        assertFalse(result);
    }

    @ParameterizedTest
    @CsvSource(
            value = {"EMPLOYER,true", "APPLICANT,false", "null,false"},
            nullValues = "null")
    void isEmployer_checksAccountType(AccountType role, boolean expected) {
        var authentication = authenticated(2L, role);

        var result = security.isEmployer(authentication);

        assertEquals(expected, result);
    }

    @ParameterizedTest
    @MethodSource("invalidAuthentications")
    void isEmployer_whenAuthenticationInvalid_returnsFalse(Authentication authentication) {
        var result = security.isEmployer(authentication);

        assertFalse(result);
    }

    @ParameterizedTest
    @CsvSource(
            value = {"2,true", "3,false", "null,false"},
            nullValues = "null")
    void employerOwnsVacancy_checksOwner(Long ownerId, boolean expected) {
        var model = vacancy();
        model.setEmployerId(ownerId);
        var authentication = authenticated(2L, AccountType.EMPLOYER);
        when(vacancies.getVacancyById(1L)).thenReturn(Optional.of(model));

        var result = security.employerOwnsVacancy(authentication, 1L);

        assertEquals(expected, result);
        verify(vacancies).getVacancyById(1L);
    }

    @Test
    void employerOwnsVacancy_whenVacancyMissing_deniesAccess() {
        var authentication = authenticated(2L, AccountType.EMPLOYER);
        when(vacancies.getVacancyById(1L)).thenReturn(Optional.empty());

        var result = security.employerOwnsVacancy(authentication, 1L);

        assertFalse(result);
    }

    @Test
    void employerOwnsVacancy_whenApplicant_skipsStorage() {
        var authentication = authenticated(2L, AccountType.APPLICANT);

        var result = security.employerOwnsVacancy(authentication, 1L);

        assertFalse(result);
        verifyNoInteractions(vacancies);
    }

    @Test
    void employerOwnsVacancy_whenStorageFails_propagatesFailure() {
        var authentication = authenticated(2L, AccountType.EMPLOYER);
        var failure = new IllegalStateException("storage unavailable");
        when(vacancies.getVacancyById(1L)).thenThrow(failure);

        var thrown =
                assertThrows(
                        IllegalStateException.class,
                        () -> security.employerOwnsVacancy(authentication, 1L));

        assertSame(failure, thrown);
    }

    @ParameterizedTest
    @CsvSource(
            value = {"2,true", "3,false", "null,false"},
            nullValues = "null")
    void applicantOwnsResume_checksOwner(Long ownerId, boolean expected) {
        var model = resume();
        model.setApplicantId(ownerId);
        var authentication = authenticated(2L, AccountType.APPLICANT);
        when(resumes.getResumeById(1L)).thenReturn(Optional.of(model));

        var result = security.applicantOwnsResume(authentication, 1L);

        assertEquals(expected, result);
        verify(resumes).getResumeById(1L);
    }

    @Test
    void applicantOwnsResume_whenResumeMissing_deniesAccess() {
        var authentication = authenticated(2L, AccountType.APPLICANT);
        when(resumes.getResumeById(1L)).thenReturn(Optional.empty());

        var result = security.applicantOwnsResume(authentication, 1L);

        assertFalse(result);
    }

    @Test
    void applicantOwnsResume_whenEmployer_skipsStorage() {
        var authentication = authenticated(2L, AccountType.EMPLOYER);

        var result = security.applicantOwnsResume(authentication, 1L);

        assertFalse(result);
        verifyNoInteractions(resumes);
    }

    @Test
    void applicantOwnsResume_whenStorageFails_propagatesFailure() {
        var authentication = authenticated(2L, AccountType.APPLICANT);
        var failure = new IllegalStateException("storage unavailable");
        when(resumes.getResumeById(1L)).thenThrow(failure);

        var thrown =
                assertThrows(
                        IllegalStateException.class,
                        () -> security.applicantOwnsResume(authentication, 1L));

        assertSame(failure, thrown);
    }

    @ParameterizedTest
    @CsvSource(
            value = {
                "APPLICANT,2,true", "APPLICANT,3,false", "APPLICANT,null,false",
                "EMPLOYER,2,true", "EMPLOYER,3,false", "EMPLOYER,null,false"
            },
            nullValues = "null")
    void canAccessApplication_checksOwnershipForEachRole(
            AccountType role, Long ownerId, boolean expected) {
        var authentication = authenticated(2L, role);
        when(applications.getApplicationById(1L)).thenReturn(Optional.of(application()));
        if (role == AccountType.APPLICANT) {
            var model = resume();
            model.setApplicantId(ownerId);
            when(resumes.getResumeById(3L)).thenReturn(Optional.of(model));
        } else {
            var model = vacancy();
            model.setEmployerId(ownerId);
            when(vacancies.getVacancyById(2L)).thenReturn(Optional.of(model));
        }

        var result = security.canAccessApplication(authentication, 1L);

        assertEquals(expected, result);
        if (role == AccountType.APPLICANT) {
            verifyNoInteractions(vacancies);
        } else {
            verifyNoInteractions(resumes);
        }
    }

    @ParameterizedTest
    @MethodSource("invalidAuthentications")
    void canAccessApplication_whenAuthenticationInvalid_skipsStorage(
            Authentication authentication) {
        var result = security.canAccessApplication(authentication, 1L);

        assertFalse(result);
        verifyNoInteractions(applications, vacancies, resumes);
    }

    @Test
    void canAccessApplication_whenApplicationMissing_deniesAccess() {
        var authentication = authenticated(2L, AccountType.APPLICANT);
        when(applications.getApplicationById(1L)).thenReturn(Optional.empty());

        var result = security.canAccessApplication(authentication, 1L);

        assertFalse(result);
        verifyNoInteractions(vacancies, resumes);
    }

    @ParameterizedTest
    @EnumSource(AccountType.class)
    void canAccessApplication_whenRelatedRecordMissing_deniesAccess(AccountType role) {
        var authentication = authenticated(2L, role);
        when(applications.getApplicationById(1L)).thenReturn(Optional.of(application()));
        if (role == AccountType.APPLICANT) {
            when(resumes.getResumeById(3L)).thenReturn(Optional.empty());
        } else {
            when(vacancies.getVacancyById(2L)).thenReturn(Optional.empty());
        }

        var result = security.canAccessApplication(authentication, 1L);

        assertFalse(result);
    }

    @Test
    void canAccessApplication_whenRoleMissing_deniesAccess() {
        var authentication = authenticated(2L, null);
        when(applications.getApplicationById(1L)).thenReturn(Optional.of(application()));

        var result = security.canAccessApplication(authentication, 1L);

        assertFalse(result);
        verifyNoInteractions(vacancies, resumes);
    }

    @Test
    void canAccessApplication_whenStorageFails_propagatesFailure() {
        var authentication = authenticated(2L, AccountType.APPLICANT);
        var failure = new IllegalStateException("storage unavailable");
        when(applications.getApplicationById(1L)).thenThrow(failure);

        var thrown =
                assertThrows(
                        IllegalStateException.class,
                        () -> security.canAccessApplication(authentication, 1L));

        assertSame(failure, thrown);
    }
}
