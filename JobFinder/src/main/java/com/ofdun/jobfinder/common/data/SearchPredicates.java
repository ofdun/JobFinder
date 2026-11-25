package com.ofdun.jobfinder.common.data;

import jakarta.persistence.criteria.*;
import java.util.ArrayList;
import java.util.List;

public final class SearchPredicates<T> {
    private final Root<T> root;
    private final CriteriaQuery<?> query;
    private final CriteriaBuilder builder;
    private final List<Predicate> predicates = new ArrayList<>();

    public SearchPredicates(Root<T> root, CriteriaQuery<?> query, CriteriaBuilder builder) {
        this.root = root;
        this.query = query;
        this.builder = builder;
    }

    public void equal(String field, Object value) {
        if (value != null) predicates.add(builder.equal(root.get(field), value));
    }

    public <V extends Comparable<? super V>> void minimum(String field, V value) {
        if (value != null) predicates.add(builder.greaterThanOrEqualTo(root.get(field), value));
    }

    public <V extends Comparable<? super V>> void maximum(String field, V value) {
        if (value != null) predicates.add(builder.lessThanOrEqualTo(root.get(field), value));
    }

    public void text(String field, String value) {
        if (value != null && !value.isBlank()) {
            predicates.add(
                    builder.like(
                            builder.lower(root.get(field)),
                            "%" + value.trim().toLowerCase() + "%"));
        }
    }

    public void containsAll(String field, List<Long> values) {
        if (values == null) return;
        for (Long value : values) {
            if (value == null) continue;
            var subquery = query.subquery(Long.class);
            var candidate = subquery.from(root.getJavaType());
            var joined = candidate.join(field, JoinType.INNER);
            subquery.select(builder.literal(1L))
                    .where(
                            builder.and(
                                    builder.equal(candidate.get("id"), root.get("id")),
                                    builder.equal(joined, value)));
            predicates.add(builder.exists(subquery));
        }
    }

    public Predicate build() {
        return builder.and(predicates.toArray(Predicate[]::new));
    }
}
