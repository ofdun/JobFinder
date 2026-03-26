package com.ofdun.jobfinder.features.resume.domain.chain.save;

import com.ofdun.jobfinder.features.resume.domain.chain.ResumeHandler;
import com.ofdun.jobfinder.features.resume.domain.model.ResumeModel;
import com.ofdun.jobfinder.features.resume.domain.repository.RelationalResumeRepository;
import com.ofdun.jobfinder.features.resume.domain.repository.VectorResumeRepository;
import org.springframework.stereotype.Component;

@Component
public class VectorResumeSaveHandler extends ResumeHandler {

    VectorResumeRepository resumeRepository;

    @Override
    protected ResumeModel execute(ResumeModel resume) {
        var id = resumeRepository.createResume(resume);
        resume.setId(id);
        return resume;
    }
}
