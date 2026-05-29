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
        var existingOpt = resumeRepository.getResumeById(resume.getId());
        if (existingOpt.isEmpty()) {
            throw new ResumeNotFoundException(resume.getId());
        }

        var existing = existingOpt.get();

        if (resume.getApplicantId() == null) {
            resume.setApplicantId(existing.getApplicantId());
        }
        if (resume.getCategoryId() == null) {
            resume.setCategoryId(existing.getCategoryId());
        }
        if (resume.getDescription() == null) {
            resume.setDescription(existing.getDescription());
        }
        if (resume.getSkillIds() == null) {
            resume.setSkillIds(existing.getSkillIds());
        }
        if (resume.getLanguageIds() == null) {
            resume.setLanguageIds(existing.getLanguageIds());
        }
        if (resume.getEducations() == null) {
            resume.setEducations(existing.getEducations());
        }
        if (resume.getJobExperiences() == null) {
            resume.setJobExperiences(existing.getJobExperiences());
        }
        if (resume.getDate() == null) {
            resume.setDate(existing.getDate());
        }

        return Optional.ofNullable(resumeRepository.updateResume(resume));
    }
}
