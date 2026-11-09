package com.ofdun.jobfinder.features.api;

import com.ofdun.jobfinder.features.application.domain.model.ApplicationModel;
import com.ofdun.jobfinder.features.application.domain.service.ApplicationService;
import com.ofdun.jobfinder.features.auth.enums.AccountType;
import com.ofdun.jobfinder.features.auth.security.JobFinderPrincipal;
import com.ofdun.jobfinder.features.resume.domain.model.ResumeModel;
import com.ofdun.jobfinder.features.resume.domain.repository.RelationalResumeRepository;
import com.ofdun.jobfinder.features.vacancy.domain.model.VacancyModel;
import com.ofdun.jobfinder.features.vacancy.domain.service.VacancyService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class ApiAccess {
    private final VacancyService vacancies;
    private final RelationalResumeRepository resumes;
    private final ApplicationService applications;

    public JobFinderPrincipal principal(Authentication auth) {
        if (auth == null
                || !auth.isAuthenticated()
                || !(auth.getPrincipal() instanceof JobFinderPrincipal))
            throw new ApiFailure("UNAUTHENTICATED", 401, "Authentication required");
        return (JobFinderPrincipal) auth.getPrincipal();
    }

    public JobFinderPrincipal role(Authentication auth, AccountType type) {
        var p = principal(auth);
        require(p.accountType() == type);
        return p;
    }

    public void self(Authentication auth, AccountType type, Long id) {
        require(role(auth, type).id().equals(id));
    }

    public VacancyModel ownVacancy(Authentication auth, Long id) {
        var p = role(auth, AccountType.EMPLOYER);
        var model = vacancies.getVacancyById(id);
        require(p.id().equals(model.getEmployerId()));
        return model;
    }

    public ResumeModel ownResume(Authentication auth, Long id) {
        var p = role(auth, AccountType.APPLICANT);
        var model = resume(id);
        require(p.id().equals(model.getApplicantId()));
        return model;
    }

    public ResumeModel readResume(Authentication auth, Long id) {
        var p = principal(auth);
        var model = resume(id);
        require(p.accountType() == AccountType.EMPLOYER || p.id().equals(model.getApplicantId()));
        return model;
    }

    private ResumeModel resume(Long id) {
        return resumes.getResumeById(id)
                .orElseThrow(() -> new ApiFailure("NOT_FOUND", 404, "Resume not found"));
    }

    public ApplicationModel application(Authentication auth, Long id) {
        var p = principal(auth);
        var model = applications.getApplication(id);
        if (p.accountType() == AccountType.EMPLOYER) ownVacancy(auth, model.getVacancyId());
        else ownResume(auth, model.getResumeId());
        return model;
    }

    public ApplicationModel employerApplication(Authentication auth, Long id) {
        role(auth, AccountType.EMPLOYER);
        return application(auth, id);
    }

    public ApplicationModel applicantApplication(Authentication auth, Long id) {
        role(auth, AccountType.APPLICANT);
        return application(auth, id);
    }

    public void newApplication(Authentication auth, Long vacancyId, Long resumeId) {
        ownResume(auth, resumeId);
        vacancies.getVacancyById(vacancyId);
        if (applications.getApplicationsByVacancyId(vacancyId).stream()
                .anyMatch(a -> resumeId.equals(a.getResumeId())))
            throw new ApiFailure("CONFLICT", 409, "Application already exists");
    }

    public static void require(boolean value) {
        if (!value) throw new ApiFailure("FORBIDDEN", 403, "Access denied");
    }

    public static Long id(Object value) {
        try {
            long id = Long.parseLong(String.valueOf(value));
            if (id <= 0) throw new NumberFormatException();
            return id;
        } catch (NumberFormatException ex) {
            throw new IllegalArgumentException("ID must be a positive Java Long");
        }
    }
}
