package com.ofdun.jobfinder.features.resume.domain.chain.save;

import com.ofdun.jobfinder.features.resume.domain.chain.ResumeHandler;
import com.ofdun.jobfinder.features.resume.domain.model.ResumeModel;
import com.ofdun.jobfinder.features.resume.domain.repository.VectorResumeRepository;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class VectorResumeSaveHandler extends ResumeHandler {
    private final VectorResumeRepository resumeRepository;

    @Override
    protected Optional<ResumeModel> execute(ResumeModel resume) {
        resumeRepository.createResume(resume);
        return Optional.of(resume);
    }
}
