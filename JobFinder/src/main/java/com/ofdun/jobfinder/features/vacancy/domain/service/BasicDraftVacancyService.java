package com.ofdun.jobfinder.features.vacancy.domain.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.ofdun.jobfinder.features.vacancy.domain.model.DraftVacancyModel;
import com.ofdun.jobfinder.features.vacancy.domain.model.VacancyModel;
import com.ofdun.jobfinder.features.vacancy.domain.repository.DraftVacancyRepository;
import com.ofdun.jobfinder.features.vacancy.domain.repository.VacancyRepository;
import com.ofdun.jobfinder.features.vacancy.enums.VacancyStatus;
import com.ofdun.jobfinder.features.vacancy.exception.VacancyNotFoundException;
import java.util.Date;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class BasicDraftVacancyService implements DraftVacancyService {
    private final DraftVacancyRepository draftVacancyRepository;
    private final VacancyRepository vacancyRepository;
    private final ObjectMapper objectMapper = new ObjectMapper().findAndRegisterModules();

    @Override
    public DraftVacancyModel createDraft(Long vacancyId) {
        var vacancy = getVacancyRequired(vacancyId);
        var snapshot = writeSnapshot(vacancy);
        var model = new DraftVacancyModel(null, vacancyId, new Date(), snapshot);
        return draftVacancyRepository.save(model);
    }

    @Override
    public List<DraftVacancyModel> getDrafts(Long vacancyId) {
        ensureVacancyExists(vacancyId);
        return draftVacancyRepository.getByVacancyId(vacancyId);
    }

    @Override
    public VacancyModel applyDraft(Long vacancyId, Long draftId) {
        var draft = getDraftRequired(vacancyId, draftId);

        var model = readSnapshot(draft.getSnapshot());
        model.setId(vacancyId);
        model.setStatus(VacancyStatus.ACTIVE);

        return vacancyRepository.updateVacancy(model);
    }

    @Override
    public void deleteDraft(Long vacancyId, Long draftId) {
        getDraftRequired(vacancyId, draftId);
        draftVacancyRepository.deleteById(draftId);
    }

    private void ensureVacancyExists(Long vacancyId) {
        if (vacancyId == null || vacancyRepository.getVacancyById(vacancyId).isEmpty()) {
            throw new VacancyNotFoundException(vacancyId);
        }
    }

    private VacancyModel getVacancyRequired(Long vacancyId) {
        return vacancyRepository.getVacancyById(vacancyId)
                .orElseThrow(() -> new VacancyNotFoundException(vacancyId));
    }

    private String writeSnapshot(VacancyModel vacancy) {
        try {
            return objectMapper.writeValueAsString(vacancy);
        } catch (JsonProcessingException ex) {
            throw new IllegalStateException("Failed to serialize vacancy snapshot", ex);
        }
    }

    private VacancyModel readSnapshot(String snapshot) {
        try {
            return objectMapper.readValue(snapshot, VacancyModel.class);
        } catch (JsonProcessingException ex) {
            throw new IllegalStateException("Failed to parse vacancy snapshot", ex);
        }
    }

    private DraftVacancyModel getDraftRequired(Long vacancyId, Long draftId) {
        var draft = draftVacancyRepository.getById(draftId)
                .orElseThrow(() -> new IllegalArgumentException("Draft not found: " + draftId));
        if (!draft.getVacancyId().equals(vacancyId)) {
            throw new IllegalArgumentException("Draft does not belong to vacancy: " + vacancyId);
        }
        return draft;
    }
}
