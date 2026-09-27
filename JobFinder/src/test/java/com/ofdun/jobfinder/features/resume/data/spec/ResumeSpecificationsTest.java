package com.ofdun.jobfinder.features.resume.data.spec;

import static com.ofdun.jobfinder.support.TestDataMother.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import com.ofdun.jobfinder.features.resume.data.postgres.entity.ResumeEntity;
import com.ofdun.jobfinder.features.resume.domain.model.ResumeSearchFilter;
import jakarta.persistence.criteria.*;
import java.util.*;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class ResumeSpecificationsTest {
    @Mock private Root<ResumeEntity> root;
    @Mock private CriteriaQuery<ResumeEntity> query;
    @Mock private CriteriaBuilder criteria;
    @Mock private Path<Object> path;
    @Mock private Predicate predicate;

    @Test
    @Tag("equivalence")
    void byFilter_whenFieldPresent_buildsEqualityPredicate() {
        var filter = new ResumeSearchFilter();
        filter.setApplicantId(42L);
        var specification = ResumeSpecifications.byFilter(filter);
        when(root.get("applicantId")).thenReturn(path);
        when(criteria.equal(path, 42L)).thenReturn(predicate);
        when(criteria.and(any(Predicate[].class))).thenReturn(predicate);

        var result = specification.toPredicate(root, query, criteria);

        assertSame(predicate, result);
        verify(criteria).equal(path, 42L);
    }

    @Test
    @Tag("equivalence")
    void byFilter_whenNull_returnsUnrestrictedPredicate() {
        var specification = ResumeSpecifications.byFilter(null);
        when(criteria.conjunction()).thenReturn(predicate);

        var result = specification.toPredicate(root, query, criteria);

        assertSame(predicate, result);
        verifyNoInteractions(root);
    }

    @Test
    @Tag("equivalence")
    void byFilter_whenAttributeUnavailable_propagatesFailure() {
        var filter = new ResumeSearchFilter();
        filter.setApplicantId(42L);
        var specification = ResumeSpecifications.byFilter(filter);
        when(root.get("applicantId")).thenThrow(new IllegalArgumentException("missing attribute"));

        var error =
                assertThrows(
                        IllegalArgumentException.class,
                        () -> specification.toPredicate(root, query, criteria));

        assertEquals("missing attribute", error.getMessage());
    }
}
