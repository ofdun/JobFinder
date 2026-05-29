package com.ofdun.jobfinder.features.education.data.mongo.repository;

import com.ofdun.jobfinder.features.education.data.mongo.mapper.EducationMongoMapper;
import com.ofdun.jobfinder.features.education.domain.model.EducationModel;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@RequiredArgsConstructor
@ConditionalOnProperty(name = "app.persistence.type", havingValue = "mongo")
public class MongoEducationRepository {
    private final MongoEducationCRUDRepository mongoRepository;

    public List<EducationModel> createEducations(List<EducationModel> educationModels) {
        return mongoRepository.saveAll(
                        educationModels.stream().map(EducationMongoMapper::toEntity).toList())
                .stream()
                .map(EducationMongoMapper::toModel)
                .toList();
    }

    public List<EducationModel> getEducationsByResumeId(Long resumeId) {
        return mongoRepository.findAllByResumeId(resumeId).stream()
                .map(EducationMongoMapper::toModel)
                .toList();
    }

    public void deleteAllByResumeId(Long resumeId) {
        mongoRepository.deleteAllByResumeId(resumeId);
    }
}

