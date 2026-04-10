package com.ofdun.jobfinder.features.resume.data.repository;

import com.ofdun.jobfinder.features.education.data.mapper.EducationMapper;
import com.ofdun.jobfinder.features.education.data.repository.EducationJpaRepository;
import com.ofdun.jobfinder.features.experience.data.mapper.JobExperienceMapper;
import com.ofdun.jobfinder.features.experience.data.repository.JobExperienceJpaRepository;
import com.ofdun.jobfinder.features.resume.data.mapper.ResumeMapper;
import com.ofdun.jobfinder.features.resume.domain.model.ResumeModel;
import com.ofdun.jobfinder.features.resume.domain.repository.RelationalResumeRepository;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
@RequiredArgsConstructor
public class PostgreSQLResumeRepository implements RelationalResumeRepository {
    private final ResumeJpaRepository resumeJpaRepository;
    private final EducationJpaRepository educationJpaRepository;
    private final JobExperienceJpaRepository jobExperienceJpaRepository;

    @Override
    @Transactional
    public Long createResume(ResumeModel resumeModel) {
        var savedResume = resumeJpaRepository.save(ResumeMapper.toEntity(resumeModel));
        var resumeId = savedResume.getId();

        if (resumeModel.getEducations() != null && !resumeModel.getEducations().isEmpty()) {
            var educationEntities = resumeModel.getEducations().stream()
                    .peek(e -> {
                        e.setId(null);
                        e.setResumeId(resumeId);
                    })
                    .map(EducationMapper::toEntity)
                    .toList();
            educationJpaRepository.saveAll(educationEntities);
        }

        if (resumeModel.getJobExperiences() != null && !resumeModel.getJobExperiences().isEmpty()) {
            var experienceEntities = resumeModel.getJobExperiences().stream()
                    .peek(e -> {
                        e.setId(null);
                        e.setResumeId(resumeId);
                    })
                    .map(JobExperienceMapper::toEntity)
                    .toList();
            jobExperienceJpaRepository.saveAll(experienceEntities);
        }

        return resumeId;
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<ResumeModel> getResumeById(Long resumeId) {
        return resumeJpaRepository.findById(resumeId).map(entity -> {
            var model = ResumeMapper.toModel(entity);
            var educations = educationJpaRepository.findAllByResumeId(resumeId).stream()
                    .map(EducationMapper::toModel)
                    .toList();
            var experiences = jobExperienceJpaRepository.findAllByResumeId(resumeId).stream()
                    .map(JobExperienceMapper::toModel)
                    .toList();
            model.setEducations(educations.isEmpty() ? null : educations);
            model.setJobExperiences(experiences.isEmpty() ? null : experiences);
            return model;
        });
    }

    @Override
    @Transactional
    public ResumeModel updateResume(ResumeModel resumeModel) {
        var savedResume = resumeJpaRepository.save(ResumeMapper.toEntity(resumeModel));
        var resumeId = savedResume.getId();

        // Пересоздаём связанные записи
        educationJpaRepository.deleteAllByResumeId(resumeId);
        jobExperienceJpaRepository.deleteAllByResumeId(resumeId);

        if (resumeModel.getEducations() != null && !resumeModel.getEducations().isEmpty()) {
            var educationEntities = resumeModel.getEducations().stream()
                    .peek(e -> {
                        e.setId(null);
                        e.setResumeId(resumeId);
                    })
                    .map(EducationMapper::toEntity)
                    .toList();
            educationJpaRepository.saveAll(educationEntities);
        }

        if (resumeModel.getJobExperiences() != null && !resumeModel.getJobExperiences().isEmpty()) {
            var experienceEntities = resumeModel.getJobExperiences().stream()
                    .peek(e -> {
                        e.setId(null);
                        e.setResumeId(resumeId);
                    })
                    .map(JobExperienceMapper::toEntity)
                    .toList();
            jobExperienceJpaRepository.saveAll(experienceEntities);
        }

        return getResumeById(resumeId).orElseGet(() -> ResumeMapper.toModel(savedResume));
    }

    @Override
    @Transactional
    public Boolean deleteResume(Long resumeId) {
        if (resumeJpaRepository.findById(resumeId).isEmpty()) {
            return false;
        }

        educationJpaRepository.deleteAllByResumeId(resumeId);
        jobExperienceJpaRepository.deleteAllByResumeId(resumeId);
        resumeJpaRepository.deleteById(resumeId);

        return true;
    }
}
