package com.ofdun.jobfinder.features.resume.domain.chain.get;

import com.ofdun.jobfinder.features.resume.domain.chain.ResumeHandler;
import com.ofdun.jobfinder.features.resume.domain.model.ResumeModel;
import com.ofdun.jobfinder.features.resume.domain.repository.RelationalResumeRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
@RequiredArgsConstructor
public class RelationalResumeGetHandler extends ResumeHandler {
    private final RelationalResumeRepository resumeRepository;

    @Override
    protected Optional<ResumeModel> execute(ResumeModel resume) {
        return resumeRepository.getResumeById(resume.getId());
    }
}
