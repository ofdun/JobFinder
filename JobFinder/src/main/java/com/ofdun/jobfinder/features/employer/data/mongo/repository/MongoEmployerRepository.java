package com.ofdun.jobfinder.features.employer.data.mongo.repository;

import com.ofdun.jobfinder.features.employer.data.mongo.mapper.EmployerMongoMapper;
import com.ofdun.jobfinder.features.employer.domain.model.EmployerModel;
import com.ofdun.jobfinder.features.employer.domain.repository.EmployerRepository;
import io.micrometer.observation.annotation.Observed;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@ConditionalOnProperty(name = "app.persistence.type", havingValue = "mongo")
public class MongoEmployerRepository implements EmployerRepository {
    private final MongoEmployerCRUDRepository mongoRepository;

    @Override
    @Observed(
            name = "jobfinder.employer.repository.createEmployer",
            contextualName = "MongoDB: создание работодателя",
            lowCardinalityKeyValues = {
                "feature",
                "employer",
                "layer",
                "repository",
                "storage",
                "MongoDB"
            })
    public Long createEmployer(EmployerModel employerModel) {
        return mongoRepository.save(EmployerMongoMapper.toEntity(employerModel)).getId();
    }

    @Override
    @Observed(
            name = "jobfinder.employer.repository.getEmployerById",
            contextualName = "MongoDB: получение работодателя по ID",
            lowCardinalityKeyValues = {
                "feature",
                "employer",
                "layer",
                "repository",
                "storage",
                "MongoDB"
            })
    public Optional<EmployerModel> getEmployerById(Long id) {
        return mongoRepository.findById(id).map(EmployerMongoMapper::toModel);
    }

    @Override
    @Observed(
            name = "jobfinder.employer.repository.getEmployerByEmail",
            contextualName = "MongoDB: получение работодателя по почте",
            lowCardinalityKeyValues = {
                "feature",
                "employer",
                "layer",
                "repository",
                "storage",
                "MongoDB"
            })
    public Optional<EmployerModel> getEmployerByEmail(String email) {
        return mongoRepository.findByEmail(email).map(EmployerMongoMapper::toModel);
    }

    @Override
    @Observed(
            name = "jobfinder.employer.repository.updateEmployer",
            contextualName = "MongoDB: обновление работодателя",
            lowCardinalityKeyValues = {
                "feature",
                "employer",
                "layer",
                "repository",
                "storage",
                "MongoDB"
            })
    public EmployerModel updateEmployer(EmployerModel employerModel) {
        var entity = mongoRepository.save(EmployerMongoMapper.toEntity(employerModel));
        return EmployerMongoMapper.toModel(entity);
    }

    @Override
    @Observed(
            name = "jobfinder.employer.repository.deleteEmployer",
            contextualName = "MongoDB: удаление работодателя",
            lowCardinalityKeyValues = {
                "feature",
                "employer",
                "layer",
                "repository",
                "storage",
                "MongoDB"
            })
    public Boolean deleteEmployer(Long id) {
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
