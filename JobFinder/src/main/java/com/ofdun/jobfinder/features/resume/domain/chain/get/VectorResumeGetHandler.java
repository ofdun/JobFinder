package com.ofdun.jobfinder.features.resume.domain.chain.get;

import com.ofdun.jobfinder.features.resume.domain.chain.ResumeHandler;
import com.ofdun.jobfinder.features.resume.domain.model.ResumeModel;
import com.ofdun.jobfinder.features.resume.domain.repository.VectorResumeRepository;
import org.springframework.stereotype.Component;

@Component
public class VectorResumeGetHandler extends ResumeHandler {

    VectorResumeRepository resumeRepository;

    @Override
    protected ResumeModel execute(ResumeModel resume) {
        return resumeRepository.getResumeById(resume.getId());
    }
}
