package com.ofdun.jobfinder.features.education.data.mongo.repository;

import com.ofdun.jobfinder.features.education.data.mongo.mapper.EducationMongoMapper;
import com.ofdun.jobfinder.features.education.domain.model.EducationModel;
import io.micrometer.observation.annotation.Observed;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@ConditionalOnProperty(name = "app.persistence.type", havingValue = "mongo")
public class MongoEducationRepository {
    private final MongoEducationCRUDRepository mongoRepository;

    @Observed(
            name = "jobfinder.education.repository.createEducations",
            contextualName = "MongoDB: создание записей образования",
            lowCardinalityKeyValues = {
                "feature",
                "education",
                "layer",
                "repository",
                "storage",
                "MongoDB"
            })
    public List<EducationModel> createEducations(List<EducationModel> educationModels) {
        return mongoRepository
                .saveAll(educationModels.stream().map(EducationMongoMapper::toEntity).toList())
                .stream()
                .map(EducationMongoMapper::toModel)
                .toList();
    }

    @Observed(
            name = "jobfinder.education.repository.getEducationsByResumeId",
            contextualName = "MongoDB: получение образования из резюме",
            lowCardinalityKeyValues = {
                "feature",
                "education",
                "layer",
                "repository",
                "storage",
                "MongoDB"
            })
    public List<EducationModel> getEducationsByResumeId(Long resumeId) {
        return mongoRepository.findAllByResumeId(resumeId).stream()
                .map(EducationMongoMapper::toModel)
                .toList();
    }

    @Observed(
            name = "jobfinder.education.repository.deleteAllByResumeId",
            contextualName = "MongoDB: удаление образования из резюме",
            lowCardinalityKeyValues = {
                "feature",
                "education",
                "layer",
                "repository",
                "storage",
                "MongoDB"
            })
    public void deleteAllByResumeId(Long resumeId) {
        mongoRepository.deleteAllByResumeId(resumeId);
    }
}
