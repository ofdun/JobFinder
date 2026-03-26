package com.ofdun.jobfinder.features.resume.domain.chain.update;

import com.ofdun.jobfinder.features.resume.domain.chain.ResumeHandler;
import com.ofdun.jobfinder.features.resume.domain.model.ResumeModel;
import com.ofdun.jobfinder.features.resume.domain.repository.RelationalResumeRepository;
import org.springframework.stereotype.Component;

@Component
public class RelationalResumeUpdateHandler extends ResumeHandler {
    RelationalResumeRepository resumeRepository;

    @Override
    protected ResumeModel execute(ResumeModel resume) {
        return resumeRepository.updateResume(resume);
    }
}
