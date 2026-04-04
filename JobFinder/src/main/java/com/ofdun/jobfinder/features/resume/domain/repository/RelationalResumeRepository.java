package com.ofdun.jobfinder.features.resume.domain.repository;

import com.ofdun.jobfinder.features.resume.domain.model.ResumeModel;

public interface RelationalResumeRepository {
    Long createResume(ResumeModel resumeModel);
    ResumeModel getResumeById(Long resumeId);
    ResumeModel updateResume(ResumeModel resumeModel);
    Boolean deleteResume(Long resumeId);
}
