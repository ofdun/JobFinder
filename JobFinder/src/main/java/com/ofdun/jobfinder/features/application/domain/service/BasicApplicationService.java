package com.ofdun.jobfinder.features.application.domain.service;

import com.ofdun.jobfinder.features.application.domain.model.ApplicationModel;
import com.ofdun.jobfinder.features.application.domain.repository.ApplicationRepository;
import com.ofdun.jobfinder.features.application.domain.validator.ApplicationValidator;
import com.ofdun.jobfinder.features.application.exception.ApplicationNotFoundException;
import io.micrometer.observation.annotation.Observed;
import jakarta.validation.Valid;
import java.util.List;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class BasicApplicationService implements ApplicationService {
    private final ApplicationRepository applicationRepository;
    private final ApplicationValidator applicationValidator;

    @Override
    @Observed(
            name = "jobfinder.application.service.getApplicationsByApplicantId",
            contextualName = "Получение откликов соискателя",
            lowCardinalityKeyValues = {"feature", "application", "layer", "service"})
    public List<ApplicationModel> getApplicationsByApplicantId(
            Long applicantId, int limit, int offset) {
        return applicationRepository.getApplicationsByApplicantId(applicantId, limit, offset);
    }

    @Override
    @Observed(
            name = "jobfinder.application.service.saveApplication",
            contextualName = "Сохранение отклика",
            lowCardinalityKeyValues = {"feature", "application", "layer", "service"})
    public Long saveApplication(@NonNull @Valid ApplicationModel application) {
        applicationValidator.validateApplicationForCreate(application);
        return applicationRepository.createApplication(application);
    }

    @Override
    @Observed(
            name = "jobfinder.application.service.getApplication",
            contextualName = "Получение отклика",
            lowCardinalityKeyValues = {"feature", "application", "layer", "service"})
    public ApplicationModel getApplication(@NonNull Long id) {
        return applicationRepository
                .getApplicationById(id)
                .orElseThrow(() -> new ApplicationNotFoundException(id));
    }

    @Override
    @Observed(
            name = "jobfinder.application.service.getApplicationsByVacancyId",
            contextualName = "Получение откликов на вакансию",
            lowCardinalityKeyValues = {"feature", "application", "layer", "service"})
    public List<ApplicationModel> getApplicationsByVacancyId(@NonNull Long vacancyId) {
        if (vacancyId <= 0) {
            throw new IllegalArgumentException("Vacancy id must be a positive number");
        }

        return applicationRepository.getApplicationsByVacancyId(vacancyId);
    }

    @Override
    @Observed(
            name = "jobfinder.application.service.updateApplication",
            contextualName = "Обновление отклика",
            lowCardinalityKeyValues = {"feature", "application", "layer", "service"})
    public ApplicationModel updateApplication(@NonNull @Valid ApplicationModel application) {
        applicationValidator.validateApplicationForUpdate(application);
        return applicationRepository.updateApplication(application);
    }

    @Override
    @Observed(
            name = "jobfinder.application.service.deleteApplication",
            contextualName = "Удаление отклика",
            lowCardinalityKeyValues = {"feature", "application", "layer", "service"})
    public Boolean deleteApplication(@NonNull Long id) {
        applicationValidator.validateApplicationForDelete(id);
        return applicationRepository.deleteApplication(id);
    }
}
