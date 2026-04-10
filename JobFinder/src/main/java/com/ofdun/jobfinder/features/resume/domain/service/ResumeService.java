package com.ofdun.jobfinder.features.resume.domain.service;

import com.ofdun.jobfinder.features.resume.domain.model.ResumeModel;

import java.util.Optional;

public interface ResumeService {
    Long createResume(ResumeModel resumeModel);
    Optional<ResumeModel> getResumeById(Long resumeId);
    Optional<ResumeModel> updateResume(ResumeModel resumeModel);
    Boolean deleteResume(Long resumeId);
}
