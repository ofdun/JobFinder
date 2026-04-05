package com.ofdun.jobfinder.features.resume.data.repository;

import com.ofdun.jobfinder.features.resume.data.mapper.ResumeMapper;
import com.ofdun.jobfinder.features.resume.domain.model.ResumeModel;
import com.ofdun.jobfinder.features.resume.domain.repository.RelationalResumeRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class PostgreSQLResumeRepository implements RelationalResumeRepository {
    ResumeJpaRepository resumeJpaRepository;

    @Override
    public Long createResume(ResumeModel resumeModel) {
        return resumeJpaRepository.save(ResumeMapper.toEntity(resumeModel)).getId();
    }

    @Override
    public ResumeModel getResumeById(Long resumeId) {
        return resumeJpaRepository.findById(resumeId).map(ResumeMapper::toModel).orElse(null);
    }

    @Override
    public ResumeModel updateResume(ResumeModel resumeModel) {
        var model = resumeJpaRepository.save(ResumeMapper.toEntity(resumeModel));
        return ResumeMapper.toModel(model);
    }

    @Override
    public Boolean deleteResume(Long resumeId) {
        return resumeJpaRepository
                .findById(resumeId)
                .map(
                        entity -> {
                            resumeJpaRepository.delete(entity);
                            return true;
                        })
                .orElse(false);
    }
}
