package com.ofdun.jobfinder.features.vacancy.data.spec;

import com.ofdun.jobfinder.common.data.SearchPredicates;
import com.ofdun.jobfinder.features.vacancy.data.postgres.entity.VacancyEntity;
import com.ofdun.jobfinder.features.vacancy.domain.model.VacancySearchFilter;
import org.springframework.data.jpa.domain.Specification;

public final class VacancySpecifications {
    private VacancySpecifications() {}

    public static Specification<VacancyEntity> byFilter(VacancySearchFilter filter) {
        return (root, query, cb) -> {
            if (filter == null) return cb.conjunction();
            var predicates = new SearchPredicates<>(root, query, cb);
            predicates.equal("employerId", filter.getEmployerId());
            predicates.equal("locationId", filter.getLocationId());
            predicates.equal("paymentFrequency", filter.getPaymentFrequency());
            predicates.equal("employmentType", filter.getEmploymentType());
            predicates.equal("workFormat", filter.getWorkFormat());
            predicates.equal("status", filter.getStatus());
            predicates.minimum("salary", filter.getSalaryMin());
            predicates.maximum("salary", filter.getSalaryMax());
            predicates.minimum("publicationDate", filter.getPublicationDateFrom());
            predicates.maximum("publicationDate", filter.getPublicationDateTo());
            predicates.text("description", filter.getQ());
            predicates.containsAll("skillIds", filter.getSkillIds());
            predicates.containsAll("languageIds", filter.getLanguageIds());
            return predicates.build();
        };
    }
}
