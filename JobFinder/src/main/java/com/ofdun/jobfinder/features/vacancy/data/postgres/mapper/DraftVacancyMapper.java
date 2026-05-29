package com.ofdun.jobfinder.features.vacancy.data.postgres.mapper;

import com.ofdun.jobfinder.features.vacancy.data.postgres.entity.DraftVacancyEntity;
import com.ofdun.jobfinder.features.vacancy.domain.model.DraftVacancyModel;

public class DraftVacancyMapper {
    public static DraftVacancyModel toModel(DraftVacancyEntity entity) {
        if (entity == null) {
            return null;
        }
        return new DraftVacancyModel(
                entity.getId(),
                entity.getVacancyId(),
                entity.getVersionTimestamp(),
                entity.getSnapshot());
    }

    public static DraftVacancyEntity toEntity(DraftVacancyModel model) {
        if (model == null) {
            return null;
        }
        return new DraftVacancyEntity(
                model.getId(),
                model.getVacancyId(),
                model.getVersionTimestamp(),
                model.getSnapshot());
    }
}
