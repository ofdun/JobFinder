package com.ofdun.jobfinder.features.vacancy.data.mongo.repository;

import com.ofdun.jobfinder.features.vacancy.data.mongo.mapper.DraftVacancyMongoMapper;
import com.ofdun.jobfinder.features.vacancy.domain.model.DraftVacancyModel;
import com.ofdun.jobfinder.features.vacancy.domain.repository.DraftVacancyRepository;
import io.micrometer.observation.annotation.Observed;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@ConditionalOnProperty(name = "app.persistence.type", havingValue = "mongo")
public class MongoDraftVacancyRepository implements DraftVacancyRepository {
    private final MongoDraftVacancyCRUDRepository mongoDraftVacancyCRUDRepository;

    @Override
    @Observed(
            name = "jobfinder.vacancy.repository.save",
            contextualName = "MongoDB: сохранение черновика вакансии",
            lowCardinalityKeyValues = {
                "feature",
                "vacancy",
                "layer",
                "repository",
                "storage",
                "MongoDB"
            })
    public DraftVacancyModel save(DraftVacancyModel model) {
        var saved = mongoDraftVacancyCRUDRepository.save(DraftVacancyMongoMapper.toEntity(model));
        return DraftVacancyMongoMapper.toModel(saved);
    }

    @Override
    @Observed(
            name = "jobfinder.vacancy.repository.getByVacancyId",
            contextualName = "MongoDB: получение черновиков вакансии",
            lowCardinalityKeyValues = {
                "feature",
                "vacancy",
                "layer",
                "repository",
                "storage",
                "MongoDB"
            })
    public List<DraftVacancyModel> getByVacancyId(Long vacancyId) {
        return mongoDraftVacancyCRUDRepository
                .findAllByVacancyIdOrderByVersionTimestampDesc(vacancyId)
                .stream()
                .map(DraftVacancyMongoMapper::toModel)
                .toList();
    }

    @Override
    @Observed(
            name = "jobfinder.vacancy.repository.getById",
            contextualName = "MongoDB: получение черновика вакансии",
            lowCardinalityKeyValues = {
                "feature",
                "vacancy",
                "layer",
                "repository",
                "storage",
                "MongoDB"
            })
    public java.util.Optional<DraftVacancyModel> getById(Long id) {
        return mongoDraftVacancyCRUDRepository.findById(id).map(DraftVacancyMongoMapper::toModel);
    }

    @Override
    @Observed(
            name = "jobfinder.vacancy.repository.deleteById",
            contextualName = "MongoDB: удаление черновика вакансии",
            lowCardinalityKeyValues = {
                "feature",
                "vacancy",
                "layer",
                "repository",
                "storage",
                "MongoDB"
            })
    public void deleteById(Long id) {
        mongoDraftVacancyCRUDRepository.deleteById(id);
    }
}
