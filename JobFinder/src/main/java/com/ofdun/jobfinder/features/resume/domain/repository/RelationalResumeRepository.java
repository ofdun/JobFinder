package com.ofdun.jobfinder.features.resume.domain.repository;

import com.ofdun.jobfinder.features.resume.domain.model.ResumeModel;

import java.util.Optional;

public interface RelationalResumeRepository {
    Long createResume(ResumeModel resumeModel);
    Optional<ResumeModel> getResumeById(Long resumeId);
    ResumeModel updateResume(ResumeModel resumeModel);
    Boolean deleteResume(Long resumeId);
}
