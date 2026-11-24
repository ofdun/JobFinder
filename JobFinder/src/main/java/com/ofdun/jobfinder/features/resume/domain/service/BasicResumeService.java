package com.ofdun.jobfinder.features.resume.domain.service;

import com.ofdun.jobfinder.common.domain.model.OffsetPagination;
import com.ofdun.jobfinder.common.domain.model.PageResult;
import com.ofdun.jobfinder.features.resume.domain.chain.EmbeddingResumeHandler;
import com.ofdun.jobfinder.features.resume.domain.chain.get.RelationalResumeGetHandler;
import com.ofdun.jobfinder.features.resume.domain.chain.get.VectorResumeGetHandler;
import com.ofdun.jobfinder.features.resume.domain.chain.save.RelationalResumeSaveHandler;
import com.ofdun.jobfinder.features.resume.domain.chain.save.VectorResumeSaveHandler;
import com.ofdun.jobfinder.features.resume.domain.chain.update.RelationalResumeUpdateHandler;
import com.ofdun.jobfinder.features.resume.domain.chain.update.VectorResumeUpdateHandler;
import com.ofdun.jobfinder.features.resume.domain.model.ResumeModel;
import com.ofdun.jobfinder.features.resume.domain.model.ResumeSearchFilter;
import com.ofdun.jobfinder.features.resume.domain.repository.RelationalResumeRepository;
import com.ofdun.jobfinder.features.resume.domain.repository.VectorResumeRepository;
import com.ofdun.jobfinder.features.resume.exception.FailedToCreateResumeException;
import io.micrometer.observation.annotation.Observed;
import jakarta.validation.Valid;
import java.util.Optional;
import java.util.function.Function;
import lombok.NonNull;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class BasicResumeService implements ResumeService {

    private final RelationalResumeRepository relationalResumeRepository;
    private final VectorResumeRepository vectorResumeRepository;

    private final Function<ResumeModel, Optional<ResumeModel>> replaceChain;
    private final Function<ResumeModel, Optional<ResumeModel>> saveChain;
    private final Function<ResumeModel, Optional<ResumeModel>> getChain;
    private final Function<ResumeModel, Optional<ResumeModel>> updateChain;

    public BasicResumeService(
            @NonNull RelationalResumeRepository relationalResumeRepository,
            @NonNull VectorResumeRepository vectorResumeRepository,
            @NonNull EmbeddingResumeHandler embeddingResumeHandler,
            @NonNull RelationalResumeSaveHandler relationalResumeSaveHandler,
            @NonNull VectorResumeSaveHandler vectorResumeSaveHandler,
            @NonNull RelationalResumeUpdateHandler relationalResumeUpdateHandler,
            @NonNull VectorResumeUpdateHandler vectorResumeUpdateHandler,
            @NonNull RelationalResumeGetHandler relationalResumeGetHandler,
            @NonNull VectorResumeGetHandler vectorResumeGetHandler) {
        this.relationalResumeRepository = relationalResumeRepository;
        this.vectorResumeRepository = vectorResumeRepository;

        replaceChain =
                model ->
                        Optional.ofNullable(relationalResumeRepository.updateResume(model))
                                .flatMap(embeddingResumeHandler::handle)
                                .flatMap(vectorResumeUpdateHandler::handle);
        saveChain =
                model ->
                        relationalResumeSaveHandler
                                .handle(model)
                                .flatMap(embeddingResumeHandler::handle)
                                .flatMap(vectorResumeSaveHandler::handle);

        updateChain =
                model ->
                        relationalResumeUpdateHandler
                                .handle(model)
                                .flatMap(embeddingResumeHandler::handle)
                                .flatMap(vectorResumeUpdateHandler::handle);

        getChain =
                model ->
                        relationalResumeGetHandler
                                .handle(model)
                                .flatMap(vectorResumeGetHandler::handle);
    }

    @Override
    @Observed(
            name = "jobfinder.resume.service.createResume",
            contextualName = "Создание резюме",
            lowCardinalityKeyValues = {"feature", "resume", "layer", "service"})
    public Long createResume(@NonNull @Valid ResumeModel resumeModel) {
        var result = saveChain.apply(resumeModel);
        return result.orElseThrow(
                        () -> new FailedToCreateResumeException(resumeModel.getApplicantId()))
                .getId();
    }

    @Override
    @Transactional(readOnly = true)
    @Observed(
            name = "jobfinder.resume.service.getResumeById",
            contextualName = "Получение резюме по ID",
            lowCardinalityKeyValues = {"feature", "resume", "layer", "service"})
    public Optional<ResumeModel> getResumeById(@NonNull Long resumeId) {
        var model = new ResumeModel();
        model.setId(resumeId);
        return getChain.apply(model);
    }

    @Override
    @Observed(
            name = "jobfinder.resume.service.replaceResume",
            contextualName = "Замена резюме",
            lowCardinalityKeyValues = {"feature", "resume", "layer", "service"})
    public Optional<ResumeModel> replaceResume(ResumeModel model) {
        var existing =
                relationalResumeRepository
                        .getResumeById(model.getId())
                        .orElseThrow(
                                () ->
                                        new com.ofdun.jobfinder.features.resume.exception
                                                .ResumeNotFoundException(model.getId()));
        if (!existing.getApplicantId().equals(model.getApplicantId())) {
            throw new IllegalArgumentException("Resume owner cannot change");
        }
        model.setDate(existing.getDate());
        return replaceChain.apply(model);
    }

    @Override
    @Observed(
            name = "jobfinder.resume.service.updateResume",
            contextualName = "Обновление резюме",
            lowCardinalityKeyValues = {"feature", "resume", "layer", "service"})
    public Optional<ResumeModel> updateResume(@NonNull @Valid ResumeModel resumeModel) {
        return updateChain.apply(resumeModel);
    }

    @Override
    @Observed(
            name = "jobfinder.resume.service.deleteResume",
            contextualName = "Удаление резюме",
            lowCardinalityKeyValues = {"feature", "resume", "layer", "service"})
    public Boolean deleteResume(@NonNull Long resumeId) {
        var relationalDelete = relationalResumeRepository.deleteResume(resumeId);

        if (!relationalDelete) {
            throw new RuntimeException("Failed to delete resume from relational repository");
        }

        var vectorDelete = vectorResumeRepository.deleteResume(resumeId);

        if (!vectorDelete) {
            throw new RuntimeException("Failed to delete resume from both repositories");
        }

        return true;
    }

    @Override
    @Transactional(readOnly = true)
    @Observed(
            name = "jobfinder.resume.service.searchResumes",
            contextualName = "Поиск резюме",
            lowCardinalityKeyValues = {"feature", "resume", "layer", "service"})
    public PageResult<ResumeModel> searchResumes(
            ResumeSearchFilter filter, OffsetPagination pagination) {
        OffsetPagination p = pagination == null ? OffsetPagination.builder().build() : pagination;

        int safeLimit = Math.min(100, Math.max(1, p.getLimit()));
        int safeOffset = Math.max(0, p.getOffset());

        return relationalResumeRepository.searchResumes(
                filter, safeLimit, safeOffset, p.getSortBy(), p.isSortDesc());
    }
}
