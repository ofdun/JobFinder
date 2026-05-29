package com.ofdun.jobfinder.features.experience.data.mongo.repository;

import com.ofdun.jobfinder.features.experience.data.mongo.mapper.JobExperienceMongoMapper;
import com.ofdun.jobfinder.features.experience.domain.model.JobExperienceModel;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@RequiredArgsConstructor
@ConditionalOnProperty(name = "app.persistence.type", havingValue = "mongo")
public class MongoJobExperienceRepository {
    private final MongoJobExperienceCRUDRepository mongoRepository;

    public Long createJobExperience(JobExperienceModel jobExperienceModel) {
        return mongoRepository.save(JobExperienceMongoMapper.toEntity(jobExperienceModel)).getId();
    }

    public List<JobExperienceModel> createJobExperiences(List<JobExperienceModel> jobExperienceModels) {
        return mongoRepository.saveAll(
                        jobExperienceModels.stream()
                                .map(JobExperienceMongoMapper::toEntity)
                                .toList())
                .stream()
                .map(JobExperienceMongoMapper::toModel)
                .toList();
    }

    public List<JobExperienceModel> getJobExperiencesByResumeId(Long resumeId) {
        return mongoRepository.findAllByResumeId(resumeId).stream()
                .map(JobExperienceMongoMapper::toModel)
                .toList();
    }

    public void deleteAllByResumeId(Long resumeId) {
        mongoRepository.deleteAllByResumeId(resumeId);
    }
}

