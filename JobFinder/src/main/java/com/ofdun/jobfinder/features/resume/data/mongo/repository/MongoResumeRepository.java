package com.ofdun.jobfinder.features.resume.data.mongo.repository;

import com.ofdun.jobfinder.common.domain.model.PageResult;
import com.ofdun.jobfinder.features.education.data.mongo.repository.MongoEducationRepository;
import com.ofdun.jobfinder.features.experience.data.mongo.repository.MongoJobExperienceRepository;
import com.ofdun.jobfinder.features.resume.data.mongo.mapper.ResumeMongoMapper;
import com.ofdun.jobfinder.features.resume.domain.model.ResumeModel;
import com.ofdun.jobfinder.features.resume.domain.model.ResumeSearchFilter;
import com.ofdun.jobfinder.features.resume.domain.repository.RelationalResumeRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

@Component
@RequiredArgsConstructor
@ConditionalOnProperty(name = "app.persistence.type", havingValue = "mongo")
public class MongoResumeRepository implements RelationalResumeRepository {
    private final MongoResumeCRUDRepository mongoResumeCRUDRepository;
    private final MongoEducationRepository mongoEducationRepository;
    private final MongoJobExperienceRepository mongoJobExperienceRepository;

    @Override
    public Long createResume(ResumeModel resumeModel) {
        var savedResume = mongoResumeCRUDRepository.save(ResumeMongoMapper.toEntity(resumeModel));
        var resumeId = savedResume.getId();

        if (resumeModel.getEducations() != null && !resumeModel.getEducations().isEmpty()) {
            resumeModel.getEducations().forEach(
                    e -> {
                        e.setId(null);
                        e.setResumeId(resumeId);
                    });
            mongoEducationRepository.createEducations(resumeModel.getEducations());
        }

        if (resumeModel.getJobExperiences() != null && !resumeModel.getJobExperiences().isEmpty()) {
            resumeModel.getJobExperiences().forEach(
                    e -> {
                        e.setId(null);
                        e.setResumeId(resumeId);
                    });
            mongoJobExperienceRepository.createJobExperiences(resumeModel.getJobExperiences());
        }

        return resumeId;
    }

    @Override
    public Optional<ResumeModel> getResumeById(Long resumeId) {
        return mongoResumeCRUDRepository
                .findById(resumeId)
                .map(
                        entity -> {
                            var model = ResumeMongoMapper.toModel(entity);
                            var educations = mongoEducationRepository.getEducationsByResumeId(resumeId);
                            var experiences =
                                    mongoJobExperienceRepository.getJobExperiencesByResumeId(resumeId);
                            model.setEducations(educations.isEmpty() ? null : educations);
                            model.setJobExperiences(experiences.isEmpty() ? null : experiences);
                            return model;
                        });
    }

    @Override
    public ResumeModel updateResume(ResumeModel resumeModel) {
        var savedResume = mongoResumeCRUDRepository.save(ResumeMongoMapper.toEntity(resumeModel));
        var resumeId = savedResume.getId();

        mongoEducationRepository.deleteAllByResumeId(resumeId);
        mongoJobExperienceRepository.deleteAllByResumeId(resumeId);

        if (resumeModel.getEducations() != null && !resumeModel.getEducations().isEmpty()) {
            resumeModel.getEducations().forEach(
                    e -> {
                        e.setId(null);
                        e.setResumeId(resumeId);
                    });
            mongoEducationRepository.createEducations(resumeModel.getEducations());
        }

        if (resumeModel.getJobExperiences() != null && !resumeModel.getJobExperiences().isEmpty()) {
            resumeModel.getJobExperiences().forEach(
                    e -> {
                        e.setId(null);
                        e.setResumeId(resumeId);
                    });
            mongoJobExperienceRepository.createJobExperiences(resumeModel.getJobExperiences());
        }

        return getResumeById(resumeId).orElseGet(() -> ResumeMongoMapper.toModel(savedResume));
    }

    @Override
    public Boolean deleteResume(Long resumeId) {
        return mongoResumeCRUDRepository
                .findById(resumeId)
                .map(
                        entity -> {
                            mongoEducationRepository.deleteAllByResumeId(resumeId);
                            mongoJobExperienceRepository.deleteAllByResumeId(resumeId);
                            mongoResumeCRUDRepository.delete(entity);
                            return true;
                        })
                .orElse(false);
    }

    @Override
    public PageResult<ResumeModel> searchResumes(
            ResumeSearchFilter filter, int limit, int offset, String sortBy, boolean sortDesc) {
        var filtered =
                mongoResumeCRUDRepository.findAll().stream()
                        .map(ResumeMongoMapper::toModel)
                        .filter(resume -> matchesFilter(resume, filter))
                        .sorted(getComparator(sortBy, sortDesc))
                        .toList();

        long total = filtered.size();
        int fromIndex = Math.min(offset, filtered.size());
        int toIndex = Math.min(fromIndex + limit, filtered.size());
        var pageItems = new ArrayList<>(filtered.subList(fromIndex, toIndex));

        int page = limit == 0 ? 0 : offset / limit;
        int totalPages = limit == 0 ? 0 : (int) Math.ceil((double) total / (double) limit);

        return new PageResult<>(pageItems, page, limit, total, totalPages);
    }

    private boolean matchesFilter(ResumeModel resume, ResumeSearchFilter filter) {
        if (resume == null) {
            return false;
        }

        if (filter == null) {
            return true;
        }

        if (filter.getApplicantId() != null
                && !Objects.equals(resume.getApplicantId(), filter.getApplicantId())) {
            return false;
        }

        if (filter.getCategoryId() != null && !Objects.equals(resume.getCategoryId(), filter.getCategoryId())) {
            return false;
        }

        if (filter.getCreationDateFrom() != null && !isAfterOrEqual(resume.getDate(), filter.getCreationDateFrom())) {
            return false;
        }

        if (filter.getCreationDateTo() != null && !isBeforeOrEqual(resume.getDate(), filter.getCreationDateTo())) {
            return false;
        }

        if (filter.getQ() != null && !filter.getQ().isBlank()) {
            var query = filter.getQ().trim().toLowerCase();
            var description = resume.getDescription() == null ? "" : resume.getDescription().toLowerCase();
            if (!description.contains(query)) {
                return false;
            }
        }

        if (!containsAll(resume.getSkillIds(), filter.getSkillIds())) {
            return false;
        }

        return containsAll(resume.getLanguageIds(), filter.getLanguageIds());
    }

    private boolean containsAll(List<Long> source, List<Long> required) {
        if (required == null || required.isEmpty()) {
            return true;
        }

        if (source == null || source.isEmpty()) {
            return false;
        }

        for (Long value : required) {
            if (value != null && !source.contains(value)) {
                return false;
            }
        }

        return true;
    }

    private boolean isAfterOrEqual(Date value, Date lowerBound) {
        return value != null && !value.before(lowerBound);
    }

    private boolean isBeforeOrEqual(Date value, Date upperBound) {
        return value != null && !value.after(upperBound);
    }

    private Comparator<ResumeModel> getComparator(String sortBy, boolean sortDesc) {
        Comparator<ResumeModel> comparator;

        if (sortBy == null || sortBy.isBlank()) {
            comparator = Comparator.comparing(ResumeModel::getId, Comparator.nullsLast(Long::compareTo));
        } else {
            comparator =
                    switch (sortBy) {
                        case "applicantId" ->
                                Comparator.comparing(
                                        ResumeModel::getApplicantId,
                                        Comparator.nullsLast(Long::compareTo));
                        case "categoryId" ->
                                Comparator.comparing(
                                        ResumeModel::getCategoryId,
                                        Comparator.nullsLast(Long::compareTo));
                        case "creationDate", "date" ->
                                Comparator.comparing(ResumeModel::getDate, Comparator.nullsLast(Date::compareTo));
                        default ->
                                Comparator.comparing(
                                        ResumeModel::getId, Comparator.nullsLast(Long::compareTo));
                    };
        }

        return sortDesc ? comparator.reversed() : comparator;
    }
}


