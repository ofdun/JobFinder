package com.ofdun.jobfinder.features.resume.data.repository;

import com.ofdun.jobfinder.features.resume.domain.model.ResumeModel;
import com.ofdun.jobfinder.features.resume.domain.repository.VectorResumeRepository;
import com.ofdun.jobfinder.shared.domain.model.MatchResultModel;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.List;

@Component
public class QdrantResumeRepository implements VectorResumeRepository {
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
    public List<MatchResultModel> getMostSimilarResumes(List<Double> embedding, Integer maxAmount) {
        return Collections.emptyList();
    }

    @Override
    public Boolean deleteResume(Long resumeId) {
        return false;
    }
}
