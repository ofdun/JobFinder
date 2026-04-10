package com.ofdun.jobfinder.features.resume.domain.repository;

import com.ofdun.jobfinder.features.resume.domain.model.ResumeModel;
import com.ofdun.jobfinder.features.matching.domain.model.MatchResultModel;
import java.util.List;
import java.util.Optional;

public interface VectorResumeRepository {
    Long createResume(ResumeModel resumeModel);

    Optional<ResumeModel> getResumeById(Long resumeId);

    ResumeModel updateResume(ResumeModel resumeModel);

    List<MatchResultModel> getMostSimilarResumes(List<Float> embedding, Integer maxAmount);

    Boolean deleteResume(Long resumeId);
}
