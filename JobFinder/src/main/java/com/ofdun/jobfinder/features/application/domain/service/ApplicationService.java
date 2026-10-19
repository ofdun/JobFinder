package com.ofdun.jobfinder.features.application.domain.service;

import com.ofdun.jobfinder.features.application.domain.model.ApplicationModel;
import java.util.List;

public interface ApplicationService {
    List<ApplicationModel> getApplicationsByApplicantId(Long applicantId, int limit, int offset);

    Long saveApplication(ApplicationModel application);

    ApplicationModel getApplication(Long id);

    List<ApplicationModel> getApplicationsByVacancyId(Long vacancyId);

    ApplicationModel updateApplication(ApplicationModel application);

    Boolean deleteApplication(Long id);
}
