package com.ofdun.jobfinder.features.resume.data.repository;

import com.ofdun.jobfinder.features.resume.domain.model.ResumeModel;
import com.ofdun.jobfinder.features.resume.domain.repository.RelationalResumeRepository;
import org.springframework.stereotype.Component;

@Component
public class PostgreSQLResumeRepository implements RelationalResumeRepository {

    @Override
    public Long createResume(ResumeModel resumeModel) {
        return 0L;
    }

    @Override
    public ResumeModel getResumeById(Long resumeId) {
        return null;
    }

    @Override
    public ResumeModel updateResume(ResumeModel resumeModel) {
        return resumeModel;
    }

    @Override
    public Boolean deleteResume(Long resumeId) {
        return false;
    }
}
