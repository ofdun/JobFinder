package com.ofdun.jobfinder.features.resume.domain.repository;

import com.ofdun.jobfinder.features.resume.domain.model.ResumeModel;
import com.ofdun.jobfinder.shared.domain.model.MatchResultModel;

import java.util.List;

public interface VectorResumeRepository {
    Long createResume(ResumeModel resumeModel);
    ResumeModel getResumeById(Long resumeId);
    ResumeModel updateResume(ResumeModel resumeModel);
    List<MatchResultModel> getMostSimilarResumes(List<Double> embedding, Integer maxAmount);
    Boolean deleteResume(Long resumeId);
}
