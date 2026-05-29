package com.ofdun.jobfinder.features.application.data.mongo.repository;

import com.ofdun.jobfinder.features.application.data.mongo.mapper.ApplicationMongoMapper;
import com.ofdun.jobfinder.features.application.domain.model.ApplicationModel;
import com.ofdun.jobfinder.features.application.domain.repository.ApplicationRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;

@Component
@RequiredArgsConstructor
@ConditionalOnProperty(name = "app.persistence.type", havingValue = "mongo")
public class MongoApplicationRepository implements ApplicationRepository {
    private final MongoApplicationCRUDRepository mongoRepository;

    @Override
    public Long createApplication(ApplicationModel application) {
        return mongoRepository.save(ApplicationMongoMapper.toEntity(application)).getId();
    }

    @Override
    public Optional<ApplicationModel> getApplicationById(Long id) {
        return mongoRepository.findById(id).map(ApplicationMongoMapper::toModel);
    }

    @Override
    public List<ApplicationModel> getApplicationsByVacancyId(Long vacancyId) {
        return mongoRepository.findByVacancyIdOrderByApplicationDateDesc(vacancyId).stream()
                .map(ApplicationMongoMapper::toModel)
                .toList();
    }

    @Override
    public ApplicationModel updateApplication(ApplicationModel application) {
        var entity = mongoRepository.save(ApplicationMongoMapper.toEntity(application));
        return ApplicationMongoMapper.toModel(entity);
    }

    @Override
    public Boolean deleteApplication(Long id) {
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

