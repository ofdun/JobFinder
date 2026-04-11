package com.ofdun.jobfinder.features.resume.data.repository;

import com.ofdun.jobfinder.common.domain.model.PageResult;
import com.ofdun.jobfinder.features.education.data.mapper.EducationMapper;
import com.ofdun.jobfinder.features.education.data.repository.EducationJpaRepository;
import com.ofdun.jobfinder.features.experience.data.mapper.JobExperienceMapper;
import com.ofdun.jobfinder.features.experience.data.repository.JobExperienceJpaRepository;
import com.ofdun.jobfinder.features.resume.data.entity.ResumeEntity;
import com.ofdun.jobfinder.features.resume.data.mapper.ResumeMapper;
import com.ofdun.jobfinder.features.resume.data.spec.ResumeSpecifications;
import com.ofdun.jobfinder.features.resume.domain.model.ResumeModel;
import com.ofdun.jobfinder.features.resume.domain.model.ResumeSearchFilter;
import com.ofdun.jobfinder.features.resume.domain.repository.RelationalResumeRepository;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
@RequiredArgsConstructor
public class PostgreSQLResumeRepository implements RelationalResumeRepository {
    private final ResumeJpaRepository resumeJpaRepository;
    private final EducationJpaRepository educationJpaRepository;
    private final JobExperienceJpaRepository jobExperienceJpaRepository;

    @PersistenceContext private EntityManager entityManager;

    @Override
    @Transactional
    public Long createResume(ResumeModel resumeModel) {
        var savedResume = resumeJpaRepository.save(ResumeMapper.toEntity(resumeModel));
        var resumeId = savedResume.getId();

        if (resumeModel.getEducations() != null && !resumeModel.getEducations().isEmpty()) {
            var educationEntities =
                    resumeModel.getEducations().stream()
                            .peek(
                                    e -> {
                                        e.setId(null);
                                        e.setResumeId(resumeId);
                                    })
                            .map(EducationMapper::toEntity)
                            .toList();
            educationJpaRepository.saveAll(educationEntities);
        }

        if (resumeModel.getJobExperiences() != null && !resumeModel.getJobExperiences().isEmpty()) {
            var experienceEntities =
                    resumeModel.getJobExperiences().stream()
                            .peek(
                                    e -> {
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
        return resumeJpaRepository
                .findById(resumeId)
                .map(
                        entity -> {
                            var model = ResumeMapper.toModel(entity);
                            var educations =
                                    educationJpaRepository.findAllByResumeId(resumeId).stream()
                                            .map(EducationMapper::toModel)
                                            .toList();
                            var experiences =
                                    jobExperienceJpaRepository.findAllByResumeId(resumeId).stream()
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

        educationJpaRepository.deleteAllByResumeId(resumeId);
        jobExperienceJpaRepository.deleteAllByResumeId(resumeId);

        if (resumeModel.getEducations() != null && !resumeModel.getEducations().isEmpty()) {
            var educationEntities =
                    resumeModel.getEducations().stream()
                            .peek(
                                    e -> {
                                        e.setId(null);
                                        e.setResumeId(resumeId);
                                    })
                            .map(EducationMapper::toEntity)
                            .toList();
            educationJpaRepository.saveAll(educationEntities);
        }

        if (resumeModel.getJobExperiences() != null && !resumeModel.getJobExperiences().isEmpty()) {
            var experienceEntities =
                    resumeModel.getJobExperiences().stream()
                            .peek(
                                    e -> {
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

    @Override
    @Transactional(readOnly = true)
    public PageResult<ResumeModel> searchResumes(
            ResumeSearchFilter filter, int limit, int offset, String sortBy, boolean sortDesc) {
        Specification<ResumeEntity> spec = ResumeSpecifications.byFilter(filter);

        long total = resumeJpaRepository.count(spec);

        var cb = entityManager.getCriteriaBuilder();

        var cq = cb.createQuery(ResumeEntity.class);
        var root = cq.from(ResumeEntity.class);
        cq.select(root);

        var predicate = spec.toPredicate(root, cq, cb);
        if (predicate != null) {
            cq.where(predicate);
        }

        if (sortBy != null && !sortBy.isBlank()) {
            cq.orderBy(sortDesc ? cb.desc(root.get(sortBy)) : cb.asc(root.get(sortBy)));
        }

        var typed = entityManager.createQuery(cq);
        typed.setFirstResult(offset);
        typed.setMaxResults(limit);

        var entities = typed.getResultList();
        var items = entities.stream().map(ResumeMapper::toModel).toList();

        int page = limit == 0 ? 0 : offset / limit;
        int totalPages = limit == 0 ? 0 : (int) Math.ceil((double) total / (double) limit);

        return new PageResult<>(items, page, limit, total, totalPages);
    }
}
