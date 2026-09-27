package com.ofdun.jobfinder.features.vacancy.data.spec;

import static com.ofdun.jobfinder.support.TestDataMother.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import com.ofdun.jobfinder.features.vacancy.data.postgres.entity.VacancyEntity;
import com.ofdun.jobfinder.features.vacancy.domain.model.VacancySearchFilter;
import jakarta.persistence.criteria.*;
import java.util.*;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class VacancySpecificationsTest {
    @Mock private Root<VacancyEntity> root;
    @Mock private CriteriaQuery<VacancyEntity> query;
    @Mock private CriteriaBuilder criteria;
    @Mock private Path<Object> path;
    @Mock private Predicate predicate;

    @Test
    @Tag("equivalence")
    void byFilter_whenFieldPresent_buildsEqualityPredicate() {
        var filter = new VacancySearchFilter();
        filter.setEmployerId(42L);
        var specification = VacancySpecifications.byFilter(filter);
        when(root.get("employerId")).thenReturn(path);
        when(criteria.equal(path, 42L)).thenReturn(predicate);
        when(criteria.and(any(Predicate[].class))).thenReturn(predicate);

        var result = specification.toPredicate(root, query, criteria);

        assertSame(predicate, result);
        verify(criteria).equal(path, 42L);
    }

    @Test
    @Tag("equivalence")
    void byFilter_whenNull_returnsUnrestrictedPredicate() {
        var specification = VacancySpecifications.byFilter(null);
        when(criteria.conjunction()).thenReturn(predicate);

        var result = specification.toPredicate(root, query, criteria);

        assertSame(predicate, result);
        verifyNoInteractions(root);
    }

    @Test
    @Tag("equivalence")
    void byFilter_whenAttributeUnavailable_propagatesFailure() {
        var filter = new VacancySearchFilter();
        filter.setEmployerId(42L);
        var specification = VacancySpecifications.byFilter(filter);
        when(root.get("employerId")).thenThrow(new IllegalArgumentException("missing attribute"));

        var error =
                assertThrows(
                        IllegalArgumentException.class,
                        () -> specification.toPredicate(root, query, criteria));

        assertEquals("missing attribute", error.getMessage());
    }
}
