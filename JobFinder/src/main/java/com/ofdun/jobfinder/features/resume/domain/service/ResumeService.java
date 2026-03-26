package com.ofdun.jobfinder.features.resume.domain.service;

import com.ofdun.jobfinder.features.resume.domain.model.ResumeModel;

public interface ResumeService {
    Long createResume(ResumeModel resumeModel);
    ResumeModel getResumeById(Long resumeId);
    ResumeModel updateResume(ResumeModel resumeModel);
    Boolean deleteResume(Long resumeId);
}
