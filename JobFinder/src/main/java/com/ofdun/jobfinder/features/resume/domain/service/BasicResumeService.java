package com.ofdun.jobfinder.features.resume.domain.service;

import com.ofdun.jobfinder.features.resume.domain.chain.EmbeddingResumeHandler;
import com.ofdun.jobfinder.features.resume.domain.chain.ResumeHandler;
import com.ofdun.jobfinder.features.resume.domain.chain.get.RelationalResumeGetHandler;
import com.ofdun.jobfinder.features.resume.domain.chain.get.VectorResumeGetHandler;
import com.ofdun.jobfinder.features.resume.domain.chain.save.RelationalResumeSaveHandler;
import com.ofdun.jobfinder.features.resume.domain.chain.save.VectorResumeSaveHandler;
import com.ofdun.jobfinder.features.resume.domain.chain.update.RelationalResumeUpdateHandler;
import com.ofdun.jobfinder.features.resume.domain.chain.update.VectorResumeUpdateHandler;
import com.ofdun.jobfinder.features.resume.domain.model.ResumeModel;
import com.ofdun.jobfinder.features.resume.domain.repository.RelationalResumeRepository;
import com.ofdun.jobfinder.features.resume.domain.repository.VectorResumeRepository;
import jakarta.validation.Valid;
import lombok.NonNull;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class BasicResumeService implements ResumeService {

    private final RelationalResumeRepository relationalResumeRepository;
    private final VectorResumeRepository vectorResumeRepository;

    private final ResumeHandler saveChain;
    private final ResumeHandler getChain;
    private final ResumeHandler updateChain;

    public BasicResumeService(@NonNull RelationalResumeRepository relationalResumeRepository,
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

        saveChain = relationalResumeSaveHandler;
        relationalResumeSaveHandler.setNext(embeddingResumeHandler).setNext(vectorResumeSaveHandler);

        updateChain = relationalResumeUpdateHandler;
        relationalResumeUpdateHandler.setNext(embeddingResumeHandler).setNext(vectorResumeUpdateHandler);

        getChain = relationalResumeGetHandler;
        relationalResumeGetHandler.setNext(vectorResumeGetHandler);
    }

    @Override
    public Long createResume(@NonNull @Valid ResumeModel resumeModel) {
        return saveChain.handle(resumeModel).getId();
    }

    @Override
    public ResumeModel getResumeById(@NonNull Long resumeId) {
        var startingResume = new ResumeModel(resumeId);
        return getChain.handle(startingResume);
    }

    @Override
    public ResumeModel updateResume(@NonNull @Valid ResumeModel resumeModel) {
        return updateChain.handle(resumeModel);
    }

    @Override
    @Transactional
    public Boolean deleteResume(@NonNull Long resumeId) {
        var relationalDelete = relationalResumeRepository.deleteResume(resumeId);

        if (!relationalDelete) {
            throw new RuntimeException("Failed to delete resume from relational repository"); // TODO
        }

        var vectorDelete = vectorResumeRepository.deleteResume(resumeId);

        if (!vectorDelete) {
            throw new RuntimeException("Failed to delete resume from both repositories"); // TODO
        }

        return true;
    }
}
