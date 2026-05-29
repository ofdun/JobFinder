package com.ofdun.jobfinder.features.vacancy.data.mongo.mapper;

import com.ofdun.jobfinder.features.vacancy.data.mongo.entity.DraftVacancyDocument;
import com.ofdun.jobfinder.features.vacancy.domain.model.DraftVacancyModel;

public class DraftVacancyMongoMapper {
    public static DraftVacancyDocument toEntity(DraftVacancyModel model) {
        if (model == null) {
            return null;
        }
        return new DraftVacancyDocument(
                model.getId(),
                model.getVacancyId(),
                model.getVersionTimestamp(),
                model.getSnapshot());
    }

    public static DraftVacancyModel toModel(DraftVacancyDocument document) {
        if (document == null) {
            return null;
        }
        return new DraftVacancyModel(
                document.getId(),
                document.getVacancyId(),
                document.getVersionTimestamp(),
                document.getSnapshot());
    }
}
