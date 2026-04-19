package com.ofdun.jobfinder.features.vacancy.data.spec;

import com.ofdun.jobfinder.features.vacancy.data.entity.VacancyEntity;
import com.ofdun.jobfinder.features.vacancy.domain.model.VacancySearchFilter;
import jakarta.persistence.criteria.JoinType;
import jakarta.persistence.criteria.Predicate;
import java.util.ArrayList;
import org.springframework.data.jpa.domain.Specification;

public final class VacancySpecifications {
    private VacancySpecifications() {}

    public static Specification<VacancyEntity> byFilter(VacancySearchFilter filter) {
        return (root, query, cb) -> {
            if (filter == null) {
                return cb.conjunction();
            }

            var predicates = new ArrayList<Predicate>();

            if (filter.getEmployerId() != null) {
                predicates.add(cb.equal(root.get("employerId"), filter.getEmployerId()));
            }
            if (filter.getLocationId() != null) {
                predicates.add(cb.equal(root.get("locationId"), filter.getLocationId()));
            }
            if (filter.getPaymentFrequency() != null) {
                predicates.add(
                        cb.equal(root.get("paymentFrequency"), filter.getPaymentFrequency()));
            }
            if (filter.getEmploymentType() != null) {
                predicates.add(cb.equal(root.get("employmentType"), filter.getEmploymentType()));
            }
            if (filter.getWorkFormat() != null) {
                predicates.add(cb.equal(root.get("workFormat"), filter.getWorkFormat()));
            }

            if (filter.getSalaryMin() != null) {
                predicates.add(cb.greaterThanOrEqualTo(root.get("salary"), filter.getSalaryMin()));
            }
            if (filter.getSalaryMax() != null) {
                predicates.add(cb.lessThanOrEqualTo(root.get("salary"), filter.getSalaryMax()));
            }

            if (filter.getPublicationDateFrom() != null) {
                predicates.add(
                        cb.greaterThanOrEqualTo(
                                root.get("publicationDate"), filter.getPublicationDateFrom()));
            }
            if (filter.getPublicationDateTo() != null) {
                predicates.add(
                        cb.lessThanOrEqualTo(
                                root.get("publicationDate"), filter.getPublicationDateTo()));
            }

            if (filter.getQ() != null && !filter.getQ().isBlank()) {
                var pattern = "%" + filter.getQ().trim().toLowerCase() + "%";
                predicates.add(cb.like(cb.lower(root.get("description")), pattern));
            }

            if (filter.getSkillIds() != null && !filter.getSkillIds().isEmpty()) {
                for (Long skillId : filter.getSkillIds()) {
                    if (skillId == null) {
                        continue;
                    }

                    var sq = query.subquery(Long.class);
                    var sqRoot = sq.from(VacancyEntity.class);
                    var sqSkill = sqRoot.join("skillIds", JoinType.INNER);
                    sq.select(cb.literal(1L))
                            .where(
                                    cb.and(
                                            cb.equal(sqRoot.get("id"), root.get("id")),
                                            cb.equal(sqSkill, skillId)));

                    predicates.add(cb.exists(sq));
                }
            }

            if (filter.getLanguageIds() != null && !filter.getLanguageIds().isEmpty()) {
                for (Long languageId : filter.getLanguageIds()) {
                    if (languageId == null) {
                        continue;
                    }

                    var sq = query.subquery(Long.class);
                    var sqRoot = sq.from(VacancyEntity.class);
                    var sqLang = sqRoot.join("languageIds", JoinType.INNER);
                    sq.select(cb.literal(1L))
                            .where(
                                    cb.and(
                                            cb.equal(sqRoot.get("id"), root.get("id")),
                                            cb.equal(sqLang, languageId)));

                    predicates.add(cb.exists(sq));
                }
            }

            return cb.and(predicates.toArray(Predicate[]::new));
        };
    }
}
