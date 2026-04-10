package com.ofdun.jobfinder.features.auth.security;

import com.ofdun.jobfinder.features.application.domain.repository.ApplicationRepository;
import com.ofdun.jobfinder.features.auth.enums.AccountType;
import com.ofdun.jobfinder.features.resume.domain.repository.RelationalResumeRepository;
import com.ofdun.jobfinder.features.vacancy.domain.repository.VacancyRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;

@Component("sec")
@RequiredArgsConstructor
public class SecurityExpressions {
    private final VacancyRepository vacancyRepository;
    private final RelationalResumeRepository resumeRepository;
    private final ApplicationRepository applicationRepository;

    public boolean isSelf(Authentication authentication, Long id) {
        if (authentication == null || !authentication.isAuthenticated()) {
            return false;
        }
        Object principal = authentication.getPrincipal();
        if (!(principal instanceof JobFinderPrincipal p)) {
            return false;
        }
        return p.id() != null && p.id().equals(id);
    }

    public boolean isApplicant(Authentication authentication) {
        return hasType(authentication, AccountType.APPLICANT);
    }

    public boolean isEmployer(Authentication authentication) {
        return hasType(authentication, AccountType.EMPLOYER);
    }

    public boolean employerOwnsVacancy(Authentication authentication, Long vacancyId) {
        if (!isEmployer(authentication)) {
            return false;
        }
        Object principal = authentication.getPrincipal();
        JobFinderPrincipal p = (JobFinderPrincipal) principal;

        return vacancyRepository
                .getVacancyById(vacancyId)
                .map(v -> v.getEmployerId() != null && p != null && v.getEmployerId().equals(p.id()))
                .orElse(false);
    }

    public boolean applicantOwnsResume(Authentication authentication, Long resumeId) {
        if (!isApplicant(authentication)) {
            return false;
        }
        Object principal = authentication.getPrincipal();
        JobFinderPrincipal p = (JobFinderPrincipal) principal;

        return resumeRepository
                .getResumeById(resumeId)
                .map(r -> r.getApplicantId() != null && p != null && r.getApplicantId().equals(p.id()))
                .orElse(false);
    }

    public boolean canAccessApplication(Authentication authentication, Long applicationId) {
        if (authentication == null || !authentication.isAuthenticated()) {
            return false;
        }
        Object principal = authentication.getPrincipal();
        if (!(principal instanceof JobFinderPrincipal p)) {
            return false;
        }

        return applicationRepository
                .getApplicationById(applicationId)
                .map(app -> {
                    if (p.accountType() == AccountType.APPLICANT) {
                        return resumeRepository
                                .getResumeById(app.getResumeId())
                                .map(r -> r.getApplicantId() != null && r.getApplicantId().equals(p.id()))
                                .orElse(false);
                    }
                    if (p.accountType() == AccountType.EMPLOYER) {
                        return vacancyRepository
                                .getVacancyById(app.getVacancyId())
                                .map(v -> v.getEmployerId() != null && v.getEmployerId().equals(p.id()))
                                .orElse(false);
                    }
                    return false;
                })
                .orElse(false);
    }

    private boolean hasType(Authentication authentication, AccountType type) {
        if (authentication == null || !authentication.isAuthenticated()) {
            return false;
        }
        Object principal = authentication.getPrincipal();
        if (!(principal instanceof JobFinderPrincipal p)) {
            return false;
        }
        return p.accountType() == type;
    }
}
