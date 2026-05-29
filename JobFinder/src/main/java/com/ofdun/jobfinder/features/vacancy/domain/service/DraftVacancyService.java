package com.ofdun.jobfinder.features.vacancy.domain.service;

import com.ofdun.jobfinder.features.vacancy.domain.model.DraftVacancyModel;
import java.util.List;

public interface DraftVacancyService {
    DraftVacancyModel createDraft(Long vacancyId);

    List<DraftVacancyModel> getDrafts(Long vacancyId);

    com.ofdun.jobfinder.features.vacancy.domain.model.VacancyModel applyDraft(
            Long vacancyId, Long draftId);

    void deleteDraft(Long vacancyId, Long draftId);
}
