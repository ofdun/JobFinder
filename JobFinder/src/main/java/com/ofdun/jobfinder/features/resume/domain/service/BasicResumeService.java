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
import lombok.NonNull;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class BasicResumeService implements ResumeService {

    RelationalResumeRepository relationalResumeRepository;
    VectorResumeRepository vectorResumeRepository;

    private ResumeHandler saveChain;
    private ResumeHandler getChain;
    private ResumeHandler updateChain;

    public BasicResumeService(EmbeddingResumeHandler embeddingResumeHandler,
                              RelationalResumeSaveHandler relationalResumeSaveHandler,
                              VectorResumeSaveHandler vectorResumeSaveHandler,
                              RelationalResumeUpdateHandler relationalResumeUpdateHandler,
                              VectorResumeUpdateHandler vectorResumeUpdateHandler,
                              RelationalResumeGetHandler relationalResumeGetHandler,
                              VectorResumeGetHandler vectorResumeGetHandler) {
        saveChain = relationalResumeSaveHandler;
        relationalResumeSaveHandler.setNext(embeddingResumeHandler).setNext(vectorResumeSaveHandler);

        updateChain = relationalResumeUpdateHandler;
        relationalResumeUpdateHandler.setNext(embeddingResumeHandler).setNext(vectorResumeUpdateHandler);

        getChain = relationalResumeGetHandler;
        relationalResumeGetHandler.setNext(vectorResumeGetHandler);
    }

    @Override
    @NonNull
    public Long createResume(ResumeModel resumeModel) {
        return saveChain.handle(resumeModel).getId();
    }

    @Override
    @NonNull
    public ResumeModel getResumeById(Long resumeId) {
        var startingResume = new ResumeModel(resumeId);
        return getChain.handle(startingResume);
    }

    @Override
    @NonNull
    public ResumeModel updateResume(ResumeModel resumeModel) {
        return updateChain.handle(resumeModel);
    }

    @Override
    @Transactional
    @NonNull
    public Boolean deleteResume(Long resumeId) {
        var relationalDelete = relationalResumeRepository.deleteResume(resumeId);
        var vectorDelete = vectorResumeRepository.deleteResume(resumeId);

        if (relationalDelete != vectorDelete) {
            throw new RuntimeException("Failed to delete resume from both repositories"); // TODO
        }

        return relationalDelete;
    }
}
