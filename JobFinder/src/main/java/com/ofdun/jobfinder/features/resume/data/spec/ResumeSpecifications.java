package com.ofdun.jobfinder.features.resume.data.spec;

import com.ofdun.jobfinder.features.resume.data.entity.ResumeEntity;
import com.ofdun.jobfinder.features.resume.domain.model.ResumeSearchFilter;
import jakarta.persistence.criteria.JoinType;
import jakarta.persistence.criteria.Predicate;
import java.util.ArrayList;
import org.springframework.data.jpa.domain.Specification;

public final class ResumeSpecifications {
    private ResumeSpecifications() {}

    public static Specification<ResumeEntity> byFilter(ResumeSearchFilter filter) {
        return (root, query, cb) -> {
            if (filter == null) {
                return cb.conjunction();
            }

            var predicates = new ArrayList<Predicate>();

            if (filter.getApplicantId() != null) {
                predicates.add(cb.equal(root.get("applicantId"), filter.getApplicantId()));
            }

            if (filter.getCategoryId() != null) {
                predicates.add(cb.equal(root.get("categoryId"), filter.getCategoryId()));
            }

            if (filter.getCreationDateFrom() != null) {
                predicates.add(
                        cb.greaterThanOrEqualTo(
                                root.get("creationDate"), filter.getCreationDateFrom()));
            }

            if (filter.getCreationDateTo() != null) {
                predicates.add(
                        cb.lessThanOrEqualTo(root.get("creationDate"), filter.getCreationDateTo()));
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
                    var sqRoot = sq.from(ResumeEntity.class);
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
                    var sqRoot = sq.from(ResumeEntity.class);
                    var sqLang = sqRoot.join("languages", JoinType.INNER);
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
