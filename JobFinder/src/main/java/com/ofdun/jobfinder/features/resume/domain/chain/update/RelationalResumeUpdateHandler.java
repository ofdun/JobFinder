package com.ofdun.jobfinder.features.resume.domain.chain.update;

import com.ofdun.jobfinder.features.resume.domain.chain.ResumeHandler;
import com.ofdun.jobfinder.features.resume.domain.model.ResumeModel;
import com.ofdun.jobfinder.features.resume.domain.repository.RelationalResumeRepository;
import com.ofdun.jobfinder.features.resume.exception.ResumeNotFoundException;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class RelationalResumeUpdateHandler extends ResumeHandler {
    private final RelationalResumeRepository resumeRepository;

    @Override
    protected Optional<ResumeModel> execute(ResumeModel resume) {
        if (resumeRepository.getResumeById(resume.getId()).isEmpty()) {
            throw new ResumeNotFoundException(resume.getId());
        }
        return Optional.ofNullable(resumeRepository.updateResume(resume));
    }
}
