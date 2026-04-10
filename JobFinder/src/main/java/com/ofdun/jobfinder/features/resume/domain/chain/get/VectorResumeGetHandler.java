package com.ofdun.jobfinder.features.resume.domain.chain.get;

import com.ofdun.jobfinder.features.resume.domain.chain.ResumeHandler;
import com.ofdun.jobfinder.features.resume.domain.model.ResumeModel;
import com.ofdun.jobfinder.features.resume.domain.repository.VectorResumeRepository;
import com.ofdun.jobfinder.features.resume.exception.ResumeNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
@RequiredArgsConstructor
public class VectorResumeGetHandler extends ResumeHandler {
    private final VectorResumeRepository resumeRepository;

    @Override
    protected Optional<ResumeModel> execute(ResumeModel resume) {
        var resumeEmbeddings = resumeRepository.getResumeById(resume.getId())
                .orElseThrow(() -> new ResumeNotFoundException(resume.getId()));
        resume.setEmbedding(resumeEmbeddings.getEmbedding());
        return Optional.of(resume);
    }
}
