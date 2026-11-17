package com.ofdun.jobfinder.features.application.data.postgres.repository;

import com.ofdun.jobfinder.features.application.data.postgres.mapper.ApplicationPostgreSQLMapper;
import com.ofdun.jobfinder.features.application.domain.model.ApplicationModel;
import com.ofdun.jobfinder.features.application.domain.repository.ApplicationRepository;
import io.micrometer.observation.annotation.Observed;
import java.util.List;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@ConditionalOnProperty(name = "app.persistence.type", havingValue = "postgres")
public class PostgreSQLApplicationRepository implements ApplicationRepository {
    private final ApplicationJpaRepository applicationJpaRepository;

    @jakarta.persistence.PersistenceContext private jakarta.persistence.EntityManager entityManager;

    @Override
    @Observed(
            name = "jobfinder.application.repository.getApplicationsByApplicantId",
            contextualName = "PostgreSQL: получение откликов соискателя",
            lowCardinalityKeyValues = {
                "feature",
                "application",
                "layer",
                "repository",
                "storage",
                "PostgreSQL"
            })
    public List<ApplicationModel> getApplicationsByApplicantId(
            Long applicantId, int limit, int offset) {
        return entityManager
                .createQuery(
                        "select a from ApplicationEntity a where a.resumeId in (select r.id from"
                            + " ResumeEntity r where r.applicantId = :applicantId) order by a.date"
                            + " desc, a.id desc",
                        com.ofdun.jobfinder.features.application.data.postgres.entity
                                .ApplicationEntity.class)
                .setParameter("applicantId", applicantId)
                .setFirstResult(offset)
                .setMaxResults(limit)
                .getResultList()
                .stream()
                .map(ApplicationPostgreSQLMapper::toModel)
                .toList();
    }

    @Override
    @Observed(
            name = "jobfinder.application.repository.createApplication",
            contextualName = "PostgreSQL: создание отклика",
            lowCardinalityKeyValues = {
                "feature",
                "application",
                "layer",
                "repository",
                "storage",
                "PostgreSQL"
            })
    public Long createApplication(ApplicationModel application) {
        return applicationJpaRepository
                .save(ApplicationPostgreSQLMapper.toEntity(application))
                .getId();
    }

    @Override
    @Observed(
            name = "jobfinder.application.repository.getApplicationById",
            contextualName = "PostgreSQL: получение отклика по ID",
            lowCardinalityKeyValues = {
                "feature",
                "application",
                "layer",
                "repository",
                "storage",
                "PostgreSQL"
            })
    public Optional<ApplicationModel> getApplicationById(Long id) {
        return applicationJpaRepository.findById(id).map(ApplicationPostgreSQLMapper::toModel);
    }

    @Override
    @Observed(
            name = "jobfinder.application.repository.getApplicationsByVacancyId",
            contextualName = "PostgreSQL: получение откликов на вакансию",
            lowCardinalityKeyValues = {
                "feature",
                "application",
                "layer",
                "repository",
                "storage",
                "PostgreSQL"
            })
    public List<ApplicationModel> getApplicationsByVacancyId(Long vacancyId) {
        return applicationJpaRepository.findByVacancyIdOrderByDateDesc(vacancyId).stream()
                .map(ApplicationPostgreSQLMapper::toModel)
                .toList();
    }

    @Override
    @Observed(
            name = "jobfinder.application.repository.updateApplication",
            contextualName = "PostgreSQL: обновление отклика",
            lowCardinalityKeyValues = {
                "feature",
                "application",
                "layer",
                "repository",
                "storage",
                "PostgreSQL"
            })
    public ApplicationModel updateApplication(ApplicationModel application) {
        var entity = ApplicationPostgreSQLMapper.toEntity(application);
        return ApplicationPostgreSQLMapper.toModel(applicationJpaRepository.save(entity));
    }

    @Override
    @Observed(
            name = "jobfinder.application.repository.deleteApplication",
            contextualName = "PostgreSQL: удаление отклика",
            lowCardinalityKeyValues = {
                "feature",
                "application",
                "layer",
                "repository",
                "storage",
                "PostgreSQL"
            })
    public Boolean deleteApplication(Long id) {
        return applicationJpaRepository
                .findById(id)
                .map(
                        entity -> {
                            applicationJpaRepository.delete(entity);
                            return true;
                        })
                .orElse(false);
    }
}
