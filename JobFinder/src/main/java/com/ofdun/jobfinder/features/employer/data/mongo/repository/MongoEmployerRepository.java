package com.ofdun.jobfinder.features.employer.data.mongo.repository;

import com.ofdun.jobfinder.features.employer.data.mongo.mapper.EmployerMongoMapper;
import com.ofdun.jobfinder.features.employer.domain.model.EmployerModel;
import com.ofdun.jobfinder.features.employer.domain.repository.EmployerRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
@RequiredArgsConstructor
@ConditionalOnProperty(name = "app.persistence.type", havingValue = "mongo")
public class MongoEmployerRepository implements EmployerRepository {
    private final MongoEmployerCRUDRepository mongoRepository;

    @Override
    public Long createEmployer(EmployerModel employerModel) {
        return mongoRepository.save(EmployerMongoMapper.toEntity(employerModel)).getId();
    }

    @Override
    public Optional<EmployerModel> getEmployerById(Long id) {
        return mongoRepository.findById(id).map(EmployerMongoMapper::toModel);
    }

    @Override
    public Optional<EmployerModel> getEmployerByEmail(String email) {
        return mongoRepository.findByEmail(email).map(EmployerMongoMapper::toModel);
    }

    @Override
    public EmployerModel updateEmployer(EmployerModel employerModel) {
        var entity = mongoRepository.save(EmployerMongoMapper.toEntity(employerModel));
        return EmployerMongoMapper.toModel(entity);
    }

    @Override
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

