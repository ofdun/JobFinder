package com.ofdun.jobfinder.features.resume.data.spec;

import com.ofdun.jobfinder.common.data.SearchPredicates;
import com.ofdun.jobfinder.features.resume.data.postgres.entity.ResumeEntity;
import com.ofdun.jobfinder.features.resume.domain.model.ResumeSearchFilter;
import org.springframework.data.jpa.domain.Specification;

public final class ResumeSpecifications {
    private ResumeSpecifications() {}

    public static Specification<ResumeEntity> byFilter(ResumeSearchFilter filter) {
        return (root, query, cb) -> {
            if (filter == null) return cb.conjunction();
            var predicates = new SearchPredicates<>(root, query, cb);
            predicates.equal("applicantId", filter.getApplicantId());
            predicates.equal("categoryId", filter.getCategoryId());
            predicates.minimum("creationDate", filter.getCreationDateFrom());
            predicates.maximum("creationDate", filter.getCreationDateTo());
            predicates.text("description", filter.getQ());
            predicates.containsAll("skillIds", filter.getSkillIds());
            predicates.containsAll("languages", filter.getLanguageIds());
            return predicates.build();
        };
    }
}
