package com.ofdun.jobfinder.features.resume.domain.chain.save;

import com.ofdun.jobfinder.features.resume.domain.chain.ResumeHandler;
import com.ofdun.jobfinder.features.resume.domain.model.ResumeModel;
import com.ofdun.jobfinder.features.resume.domain.repository.RelationalResumeRepository;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class RelationalResumeSaveHandler extends ResumeHandler {
    private final RelationalResumeRepository resumeRepository;

    @Override
    protected Optional<ResumeModel> execute(ResumeModel resume) {
        var id = resumeRepository.createResume(resume);
        resume.setId(id);
        return Optional.of(resume);
    }
}
