package com.ofdun.jobfinder.features.resume.domain.chain.save;

import com.ofdun.jobfinder.features.resume.domain.chain.ResumeHandler;
import com.ofdun.jobfinder.features.resume.domain.model.ResumeModel;
import com.ofdun.jobfinder.features.resume.domain.repository.RelationalResumeRepository;
import org.springframework.stereotype.Component;

@Component
public class RelationalResumeSaveHandler extends ResumeHandler {
    RelationalResumeRepository resumeRepository;

    @Override
    protected ResumeModel execute(ResumeModel resume) {
        var id = resumeRepository.createResume(resume);
        resume.setId(id);
        return resume;
    }
}
