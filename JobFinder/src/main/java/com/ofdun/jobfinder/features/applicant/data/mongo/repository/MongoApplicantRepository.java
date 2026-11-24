package com.ofdun.jobfinder.features.applicant.data.mongo.repository;

import com.ofdun.jobfinder.features.applicant.data.mongo.mapper.ApplicantMongoMapper;
import com.ofdun.jobfinder.features.applicant.domain.model.ApplicantModel;
import com.ofdun.jobfinder.features.applicant.domain.repository.ApplicantRepository;
import io.micrometer.observation.annotation.Observed;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@ConditionalOnProperty(name = "app.persistence.type", havingValue = "mongo")
public class MongoApplicantRepository implements ApplicantRepository {
    private final MongoApplicantCRUDRepository mongoRepository;

    @Override
    @Observed(
            name = "jobfinder.applicant.repository.createApplicant",
            contextualName = "MongoDB: создание соискателя",
            lowCardinalityKeyValues = {
                "feature",
                "applicant",
                "layer",
                "repository",
                "storage",
                "MongoDB"
            })
    public Long createApplicant(ApplicantModel applicantModel) {
        return mongoRepository.save(ApplicantMongoMapper.toEntity(applicantModel)).getId();
    }

    @Override
    @Observed(
            name = "jobfinder.applicant.repository.getApplicantById",
            contextualName = "MongoDB: получение соискателя по ID",
            lowCardinalityKeyValues = {
                "feature",
                "applicant",
                "layer",
                "repository",
                "storage",
                "MongoDB"
            })
    public Optional<ApplicantModel> getApplicantById(Long id) {
        return mongoRepository.findById(id).map(ApplicantMongoMapper::toModel);
    }

    @Override
    @Observed(
            name = "jobfinder.applicant.repository.getApplicantByEmail",
            contextualName = "MongoDB: получение соискателя по почте",
            lowCardinalityKeyValues = {
                "feature",
                "applicant",
                "layer",
                "repository",
                "storage",
                "MongoDB"
            })
    public Optional<ApplicantModel> getApplicantByEmail(String email) {
        return mongoRepository.findByEmail(email).map(ApplicantMongoMapper::toModel);
    }

    @Override
    @Observed(
            name = "jobfinder.applicant.repository.updateApplicant",
            contextualName = "MongoDB: обновление соискателя",
            lowCardinalityKeyValues = {
                "feature",
                "applicant",
                "layer",
                "repository",
                "storage",
                "MongoDB"
            })
    public ApplicantModel updateApplicant(ApplicantModel applicantModel) {
        var entity = mongoRepository.save(ApplicantMongoMapper.toEntity(applicantModel));
        return ApplicantMongoMapper.toModel(entity);
    }

    @Override
    @Observed(
            name = "jobfinder.applicant.repository.deleteApplicant",
            contextualName = "MongoDB: удаление соискателя",
            lowCardinalityKeyValues = {
                "feature",
                "applicant",
                "layer",
                "repository",
                "storage",
                "MongoDB"
            })
    public Boolean deleteApplicant(Long id) {
        return mongoRepository
                .findById(id)
                .map(
                        entity -> {
                            mongoRepository.delete(entity);
                            return true;
                        })
                .orElse(false);
    }
}
