package com.ofdun.jobfinder.features.experience.data.mongo.repository;

import com.ofdun.jobfinder.features.experience.data.mongo.mapper.JobExperienceMongoMapper;
import com.ofdun.jobfinder.features.experience.domain.model.JobExperienceModel;
import io.micrometer.observation.annotation.Observed;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@ConditionalOnProperty(name = "app.persistence.type", havingValue = "mongo")
public class MongoJobExperienceRepository {
    private final MongoJobExperienceCRUDRepository mongoRepository;

    @Observed(
            name = "jobfinder.experience.repository.createJobExperience",
            contextualName = "MongoDB: создание опыта работы",
            lowCardinalityKeyValues = {
                "feature",
                "experience",
                "layer",
                "repository",
                "storage",
                "MongoDB"
            })
    public Long createJobExperience(JobExperienceModel jobExperienceModel) {
        return mongoRepository.save(JobExperienceMongoMapper.toEntity(jobExperienceModel)).getId();
    }

    @Observed(
            name = "jobfinder.experience.repository.createJobExperiences",
            contextualName = "MongoDB: создание записей опыта работы",
            lowCardinalityKeyValues = {
                "feature",
                "experience",
                "layer",
                "repository",
                "storage",
                "MongoDB"
            })
    public List<JobExperienceModel> createJobExperiences(
            List<JobExperienceModel> jobExperienceModels) {
        return mongoRepository
                .saveAll(
                        jobExperienceModels.stream()
                                .map(JobExperienceMongoMapper::toEntity)
                                .toList())
                .stream()
                .map(JobExperienceMongoMapper::toModel)
                .toList();
    }

    @Observed(
            name = "jobfinder.experience.repository.getJobExperiencesByResumeId",
            contextualName = "MongoDB: получение опыта работы из резюме",
            lowCardinalityKeyValues = {
                "feature",
                "experience",
                "layer",
                "repository",
                "storage",
                "MongoDB"
            })
    public List<JobExperienceModel> getJobExperiencesByResumeId(Long resumeId) {
        return mongoRepository.findAllByResumeId(resumeId).stream()
                .map(JobExperienceMongoMapper::toModel)
                .toList();
    }

    @Observed(
            name = "jobfinder.experience.repository.deleteAllByResumeId",
            contextualName = "MongoDB: удаление опыта работы из резюме",
            lowCardinalityKeyValues = {
                "feature",
                "experience",
                "layer",
                "repository",
                "storage",
                "MongoDB"
            })
    public void deleteAllByResumeId(Long resumeId) {
        mongoRepository.deleteAllByResumeId(resumeId);
    }
}
