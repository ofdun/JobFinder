package com.ofdun.jobfinder.features.resume.domain.chain.get;

import com.ofdun.jobfinder.features.resume.domain.chain.ResumeHandler;
import com.ofdun.jobfinder.features.resume.domain.model.ResumeModel;
import com.ofdun.jobfinder.features.resume.domain.repository.RelationalResumeRepository;
import org.springframework.stereotype.Component;

@Component
public class RelationalResumeGetHandler extends ResumeHandler {

    RelationalResumeRepository resumeRepository;

    @Override
    protected ResumeModel execute(ResumeModel resume) {
        return resumeRepository.getResumeById(resume.getId());
    }
}
