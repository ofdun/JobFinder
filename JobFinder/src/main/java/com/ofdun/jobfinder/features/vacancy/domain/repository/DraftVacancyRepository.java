package com.ofdun.jobfinder.features.vacancy.domain.repository;

import com.ofdun.jobfinder.features.vacancy.domain.model.DraftVacancyModel;
import java.util.List;

public interface DraftVacancyRepository {
    DraftVacancyModel save(DraftVacancyModel model);

    List<DraftVacancyModel> getByVacancyId(Long vacancyId);

    java.util.Optional<DraftVacancyModel> getById(Long id);

    void deleteById(Long id);
}
