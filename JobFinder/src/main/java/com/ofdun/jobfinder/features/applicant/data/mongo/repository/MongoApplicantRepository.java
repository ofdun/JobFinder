package com.ofdun.jobfinder.features.applicant.data.mongo.repository;

import com.ofdun.jobfinder.features.applicant.data.mongo.mapper.ApplicantMongoMapper;
import com.ofdun.jobfinder.features.applicant.domain.model.ApplicantModel;
import com.ofdun.jobfinder.features.applicant.domain.repository.ApplicantRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
@RequiredArgsConstructor
@ConditionalOnProperty(name = "app.persistence.type", havingValue = "mongo")
public class MongoApplicantRepository implements ApplicantRepository {
    private final MongoApplicantCRUDRepository mongoRepository;

    @Override
    public Long createApplicant(ApplicantModel applicantModel) {
        return mongoRepository.save(
                ApplicantMongoMapper.toEntity(applicantModel)
        ).getId();
    }

    @Override
    public Optional<ApplicantModel> getApplicantById(Long id) {
        return mongoRepository.findById(id)
                .map(ApplicantMongoMapper::toModel);
    }

    @Override
    public Optional<ApplicantModel> getApplicantByEmail(String email) {
        return mongoRepository.findByEmail(email)
                .map(ApplicantMongoMapper::toModel);
    }

    @Override
    public ApplicantModel updateApplicant(ApplicantModel applicantModel) {
        var entity = mongoRepository.save(
                ApplicantMongoMapper.toEntity(applicantModel)
        );
        return ApplicantMongoMapper.toModel(entity);
    }

    @Override
    public Boolean deleteApplicant(Long id) {
        return mongoRepository.findById(id)
                .map(
                        entity -> {
                            mongoRepository.delete(entity);
                            return true;
                        }
                )
                .orElse(false);
    }
}
