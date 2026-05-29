package com.ofdun.jobfinder.features.vacancy.data.mongo.repository;

import com.ofdun.jobfinder.features.vacancy.data.mongo.mapper.DraftVacancyMongoMapper;
import com.ofdun.jobfinder.features.vacancy.domain.model.DraftVacancyModel;
import com.ofdun.jobfinder.features.vacancy.domain.repository.DraftVacancyRepository;
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
    public DraftVacancyModel save(DraftVacancyModel model) {
        var saved = mongoDraftVacancyCRUDRepository.save(DraftVacancyMongoMapper.toEntity(model));
        return DraftVacancyMongoMapper.toModel(saved);
    }

    @Override
    public List<DraftVacancyModel> getByVacancyId(Long vacancyId) {
        return mongoDraftVacancyCRUDRepository
                .findAllByVacancyIdOrderByVersionTimestampDesc(vacancyId)
                .stream()
                .map(DraftVacancyMongoMapper::toModel)
                .toList();
    }

    @Override
    public java.util.Optional<DraftVacancyModel> getById(Long id) {
        return mongoDraftVacancyCRUDRepository.findById(id).map(DraftVacancyMongoMapper::toModel);
    }

    @Override
    public void deleteById(Long id) {
        mongoDraftVacancyCRUDRepository.deleteById(id);
    }
}
