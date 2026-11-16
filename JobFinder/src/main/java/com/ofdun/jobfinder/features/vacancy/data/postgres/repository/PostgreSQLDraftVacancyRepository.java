package com.ofdun.jobfinder.features.vacancy.data.postgres.repository;

import com.ofdun.jobfinder.features.vacancy.data.postgres.mapper.DraftVacancyMapper;
import com.ofdun.jobfinder.features.vacancy.domain.model.DraftVacancyModel;
import com.ofdun.jobfinder.features.vacancy.domain.repository.DraftVacancyRepository;
import io.micrometer.observation.annotation.Observed;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@ConditionalOnProperty(name = "app.persistence.type", havingValue = "postgres")
public class PostgreSQLDraftVacancyRepository implements DraftVacancyRepository {
    private final DraftVacancyJpaRepository draftVacancyJpaRepository;

    @Override
    @Observed(
            name = "jobfinder.vacancy.repository.save",
            contextualName = "PostgreSQL: сохранение черновика вакансии",
            lowCardinalityKeyValues = {
                "feature",
                "vacancy",
                "layer",
                "repository",
                "storage",
                "PostgreSQL"
            })
    public DraftVacancyModel save(DraftVacancyModel model) {
        var saved = draftVacancyJpaRepository.save(DraftVacancyMapper.toEntity(model));
        return DraftVacancyMapper.toModel(saved);
    }

    @Override
    @Observed(
            name = "jobfinder.vacancy.repository.getByVacancyId",
            contextualName = "PostgreSQL: получение черновиков вакансии",
            lowCardinalityKeyValues = {
                "feature",
                "vacancy",
                "layer",
                "repository",
                "storage",
                "PostgreSQL"
            })
    public List<DraftVacancyModel> getByVacancyId(Long vacancyId) {
        return draftVacancyJpaRepository
                .findAllByVacancyIdOrderByVersionTimestampDesc(vacancyId)
                .stream()
                .map(DraftVacancyMapper::toModel)
                .toList();
    }

    @Override
    @Observed(
            name = "jobfinder.vacancy.repository.getById",
            contextualName = "PostgreSQL: получение черновика вакансии",
            lowCardinalityKeyValues = {
                "feature",
                "vacancy",
                "layer",
                "repository",
                "storage",
                "PostgreSQL"
            })
    public java.util.Optional<DraftVacancyModel> getById(Long id) {
        return draftVacancyJpaRepository.findById(id).map(DraftVacancyMapper::toModel);
    }

    @Override
    @Observed(
            name = "jobfinder.vacancy.repository.deleteById",
            contextualName = "PostgreSQL: удаление черновика вакансии",
            lowCardinalityKeyValues = {
                "feature",
                "vacancy",
                "layer",
                "repository",
                "storage",
                "PostgreSQL"
            })
    public void deleteById(Long id) {
        draftVacancyJpaRepository.deleteById(id);
    }
}
