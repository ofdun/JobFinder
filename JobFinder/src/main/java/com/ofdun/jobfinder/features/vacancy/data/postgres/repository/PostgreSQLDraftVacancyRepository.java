package com.ofdun.jobfinder.features.vacancy.data.postgres.repository;

import com.ofdun.jobfinder.features.vacancy.data.postgres.mapper.DraftVacancyMapper;
import com.ofdun.jobfinder.features.vacancy.domain.model.DraftVacancyModel;
import com.ofdun.jobfinder.features.vacancy.domain.repository.DraftVacancyRepository;
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
    public DraftVacancyModel save(DraftVacancyModel model) {
        var saved = draftVacancyJpaRepository.save(DraftVacancyMapper.toEntity(model));
        return DraftVacancyMapper.toModel(saved);
    }

    @Override
    public List<DraftVacancyModel> getByVacancyId(Long vacancyId) {
        return draftVacancyJpaRepository.findAllByVacancyIdOrderByVersionTimestampDesc(vacancyId)
                .stream()
                .map(DraftVacancyMapper::toModel)
                .toList();
    }

    @Override
    public java.util.Optional<DraftVacancyModel> getById(Long id) {
        return draftVacancyJpaRepository.findById(id).map(DraftVacancyMapper::toModel);
    }

    @Override
    public void deleteById(Long id) {
        draftVacancyJpaRepository.deleteById(id);
    }
}
