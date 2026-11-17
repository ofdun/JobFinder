package com.ofdun.jobfinder.features.application.data.mongo.repository;

import com.ofdun.jobfinder.features.application.data.mongo.mapper.ApplicationMongoMapper;
import com.ofdun.jobfinder.features.application.domain.model.ApplicationModel;
import com.ofdun.jobfinder.features.application.domain.repository.ApplicationRepository;
import io.micrometer.observation.annotation.Observed;
import java.util.List;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@ConditionalOnProperty(name = "app.persistence.type", havingValue = "mongo")
public class MongoApplicationRepository implements ApplicationRepository {
    private final MongoApplicationCRUDRepository mongoRepository;

    @org.springframework.beans.factory.annotation.Autowired
    private org.springframework.data.mongodb.core.MongoTemplate mongoTemplate;

    @Override
    @Observed(
            name = "jobfinder.application.repository.getApplicationsByApplicantId",
            contextualName = "MongoDB: получение откликов соискателя",
            lowCardinalityKeyValues = {
                "feature",
                "application",
                "layer",
                "repository",
                "storage",
                "MongoDB"
            })
    public List<ApplicationModel> getApplicationsByApplicantId(
            Long applicantId, int limit, int offset) {
        var resumes =
                mongoTemplate.find(
                        org.springframework.data.mongodb.core.query.Query.query(
                                org.springframework.data.mongodb.core.query.Criteria.where(
                                                "applicantId")
                                        .is(applicantId)),
                        com.ofdun.jobfinder.features.resume.data.mongo.entity.ResumeDocument.class);
        var ids =
                resumes.stream()
                        .map(
                                com.ofdun.jobfinder.features.resume.data.mongo.entity.ResumeDocument
                                        ::getId)
                        .toList();
        var query =
                org.springframework.data.mongodb.core.query.Query.query(
                                org.springframework.data.mongodb.core.query.Criteria.where(
                                                "resumeId")
                                        .in(ids))
                        .with(
                                org.springframework.data.domain.Sort.by(
                                        org.springframework.data.domain.Sort.Direction.DESC,
                                        "applicationDate",
                                        "id"))
                        .skip(offset)
                        .limit(limit);
        return mongoTemplate
                .find(
                        query,
                        com.ofdun.jobfinder.features.application.data.mongo.entity
                                .ApplicationDocument.class)
                .stream()
                .map(ApplicationMongoMapper::toModel)
                .toList();
    }

    @Override
    @Observed(
            name = "jobfinder.application.repository.createApplication",
            contextualName = "MongoDB: создание отклика",
            lowCardinalityKeyValues = {
                "feature",
                "application",
                "layer",
                "repository",
                "storage",
                "MongoDB"
            })
    public Long createApplication(ApplicationModel application) {
        return mongoRepository.save(ApplicationMongoMapper.toEntity(application)).getId();
    }

    @Override
    @Observed(
            name = "jobfinder.application.repository.getApplicationById",
            contextualName = "MongoDB: получение отклика по ID",
            lowCardinalityKeyValues = {
                "feature",
                "application",
                "layer",
                "repository",
                "storage",
                "MongoDB"
            })
    public Optional<ApplicationModel> getApplicationById(Long id) {
        return mongoRepository.findById(id).map(ApplicationMongoMapper::toModel);
    }

    @Override
    @Observed(
            name = "jobfinder.application.repository.getApplicationsByVacancyId",
            contextualName = "MongoDB: получение откликов на вакансию",
            lowCardinalityKeyValues = {
                "feature",
                "application",
                "layer",
                "repository",
                "storage",
                "MongoDB"
            })
    public List<ApplicationModel> getApplicationsByVacancyId(Long vacancyId) {
        return mongoRepository.findByVacancyIdOrderByApplicationDateDesc(vacancyId).stream()
                .map(ApplicationMongoMapper::toModel)
                .toList();
    }

    @Override
    @Observed(
            name = "jobfinder.application.repository.updateApplication",
            contextualName = "MongoDB: обновление отклика",
            lowCardinalityKeyValues = {
                "feature",
                "application",
                "layer",
                "repository",
                "storage",
                "MongoDB"
            })
    public ApplicationModel updateApplication(ApplicationModel application) {
        var entity = mongoRepository.save(ApplicationMongoMapper.toEntity(application));
        return ApplicationMongoMapper.toModel(entity);
    }

    @Override
    @Observed(
            name = "jobfinder.application.repository.deleteApplication",
            contextualName = "MongoDB: удаление отклика",
            lowCardinalityKeyValues = {
                "feature",
                "application",
                "layer",
                "repository",
                "storage",
                "MongoDB"
            })
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
