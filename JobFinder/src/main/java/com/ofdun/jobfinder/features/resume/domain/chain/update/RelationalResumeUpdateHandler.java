package com.ofdun.jobfinder.features.resume.domain.chain.update;

import com.ofdun.jobfinder.features.resume.domain.chain.ResumeHandler;
import com.ofdun.jobfinder.features.resume.domain.model.ResumeModel;
import com.ofdun.jobfinder.features.resume.domain.repository.RelationalResumeRepository;
import com.ofdun.jobfinder.features.resume.exception.ResumeNotFoundException;
import org.springframework.stereotype.Component;

@Component
public class RelationalResumeUpdateHandler extends ResumeHandler {
    RelationalResumeRepository resumeRepository;

    @Override
    protected ResumeModel execute(ResumeModel resume) {
        if (resumeRepository.getResumeById(resume.getId()) == null) {
            throw new ResumeNotFoundException(resume.getId());
        }
        return resumeRepository.updateResume(resume);
    }
}
