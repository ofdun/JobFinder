package com.ofdun.jobfinder.features.vacancy.data.mongo.repository;

import com.ofdun.jobfinder.common.domain.model.OffsetPagination;
import com.ofdun.jobfinder.common.domain.model.PageResult;
import com.ofdun.jobfinder.features.vacancy.data.mongo.mapper.VacancyMongoMapper;
import com.ofdun.jobfinder.features.vacancy.domain.model.VacancyModel;
import com.ofdun.jobfinder.features.vacancy.domain.model.VacancySearchFilter;
import com.ofdun.jobfinder.features.vacancy.domain.repository.VacancyRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

@Component
@RequiredArgsConstructor
@ConditionalOnProperty(name = "app.persistence.type", havingValue = "mongo")
public class MongoVacancyRepository implements VacancyRepository {
    private final MongoVacancyCRUDRepository mongoRepository;

    @Override
    public Long createVacancy(VacancyModel vacancyModel) {
        return mongoRepository.save(VacancyMongoMapper.toEntity(vacancyModel)).getId();
    }

    @Override
    public Optional<VacancyModel> getVacancyById(Long id) {
        return mongoRepository.findById(id).map(VacancyMongoMapper::toModel);
    }

    @Override
    public VacancyModel updateVacancy(VacancyModel vacancyModel) {
        var saved = mongoRepository.save(VacancyMongoMapper.toEntity(vacancyModel));
        return VacancyMongoMapper.toModel(saved);
    }

    @Override
    public Boolean deleteVacancy(Long id) {
        return mongoRepository
                .findById(id)
                .map(
                        entity -> {
                            mongoRepository.delete(entity);
                            return true;
                        })
                .orElse(false);
    }

    @Override
    public PageResult<VacancyModel> searchVacancies(
            VacancySearchFilter filter, OffsetPagination pagination) {
        OffsetPagination p = pagination == null ? OffsetPagination.builder().build() : pagination;

        int limit = p.getLimit();
        int offset = p.getOffset();

        var filtered =
                mongoRepository.findAll().stream()
                        .map(VacancyMongoMapper::toModel)
                        .filter(vacancy -> matchesFilter(vacancy, filter))
                        .sorted(getComparator(p.getSortBy(), p.isSortDesc()))
                        .toList();

        long total = filtered.size();
        int fromIndex = Math.min(offset, filtered.size());
        int toIndex = Math.min(fromIndex + limit, filtered.size());
        var pageItems = new ArrayList<>(filtered.subList(fromIndex, toIndex));

        int page = limit == 0 ? 0 : offset / limit;
        int totalPages = limit == 0 ? 0 : (int) Math.ceil((double) total / (double) limit);

        return new PageResult<>(pageItems, page, limit, total, totalPages);
    }

    private boolean matchesFilter(VacancyModel vacancy, VacancySearchFilter filter) {
        if (vacancy == null) {
            return false;
        }

        if (filter == null) {
            return true;
        }

        if (filter.getEmployerId() != null
                && !Objects.equals(vacancy.getEmployerId(), filter.getEmployerId())) {
            return false;
        }

        if (filter.getLocationId() != null
                && !Objects.equals(vacancy.getLocationId(), filter.getLocationId())) {
            return false;
        }

        if (filter.getPaymentFrequency() != null
                && !Objects.equals(vacancy.getPaymentFrequency(), filter.getPaymentFrequency())) {
            return false;
        }

        if (filter.getEmploymentType() != null
                && !Objects.equals(vacancy.getEmploymentType(), filter.getEmploymentType())) {
            return false;
        }

        if (filter.getWorkFormat() != null
                && !Objects.equals(vacancy.getJobFormat(), filter.getWorkFormat())) {
            return false;
        }

        if (filter.getStatus() != null
                && !Objects.equals(vacancy.getStatus(), filter.getStatus())) {
            return false;
        }

        if (!isSalaryGte(vacancy.getSalary(), filter.getSalaryMin())) {
            return false;
        }

        if (!isSalaryLte(vacancy.getSalary(), filter.getSalaryMax())) {
            return false;
        }

        if (filter.getPublicationDateFrom() != null
                && !isAfterOrEqual(vacancy.getPublicationDate(), filter.getPublicationDateFrom())) {
            return false;
        }

        if (filter.getPublicationDateTo() != null
                && !isBeforeOrEqual(vacancy.getPublicationDate(), filter.getPublicationDateTo())) {
            return false;
        }

        if (filter.getQ() != null && !filter.getQ().isBlank()) {
            var query = filter.getQ().trim().toLowerCase();
            var description = vacancy.getDescription() == null ? "" : vacancy.getDescription().toLowerCase();
            if (!description.contains(query)) {
                return false;
            }
        }

        if (!containsAll(vacancy.getSkillIds(), filter.getSkillIds())) {
            return false;
        }

        return containsAll(vacancy.getLanguageIds(), filter.getLanguageIds());
    }

    private boolean containsAll(List<Long> source, List<Long> required) {
        if (required == null || required.isEmpty()) {
            return true;
        }

        if (source == null || source.isEmpty()) {
            return false;
        }

        for (Long value : required) {
            if (value != null && !source.contains(value)) {
                return false;
            }
        }

        return true;
    }

    private boolean isSalaryGte(BigDecimal value, BigDecimal min) {
        if (min == null) {
            return true;
        }
        return value != null && value.compareTo(min) >= 0;
    }

    private boolean isSalaryLte(BigDecimal value, BigDecimal max) {
        if (max == null) {
            return true;
        }
        return value != null && value.compareTo(max) <= 0;
    }

    private boolean isAfterOrEqual(Date value, Date lowerBound) {
        return value != null && !value.before(lowerBound);
    }

    private boolean isBeforeOrEqual(Date value, Date upperBound) {
        return value != null && !value.after(upperBound);
    }

    private Comparator<VacancyModel> getComparator(String sortBy, boolean sortDesc) {
        Comparator<VacancyModel> comparator;

        if (sortBy == null || sortBy.isBlank()) {
            comparator =
                    Comparator.comparing(
                            VacancyModel::getId, Comparator.nullsLast(Long::compareTo));
        } else {
            comparator =
                    switch (sortBy) {
                        case "salary" ->
                                Comparator.comparing(
                                        VacancyModel::getSalary,
                                        Comparator.nullsLast(BigDecimal::compareTo));
                        case "publicationDate" ->
                                Comparator.comparing(
                                        VacancyModel::getPublicationDate,
                                        Comparator.nullsLast(Date::compareTo));
                        case "employerId" ->
                                Comparator.comparing(
                                        VacancyModel::getEmployerId,
                                        Comparator.nullsLast(Long::compareTo));
                        case "locationId" ->
                                Comparator.comparing(
                                        VacancyModel::getLocationId,
                                        Comparator.nullsLast(Long::compareTo));
                        default ->
                                Comparator.comparing(
                                        VacancyModel::getId, Comparator.nullsLast(Long::compareTo));
                    };
        }

        return sortDesc ? comparator.reversed() : comparator;
    }
}

