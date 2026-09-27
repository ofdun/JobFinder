package com.ofdun.jobfinder.features.resume.data.postgres.repository;

import static com.ofdun.jobfinder.support.TestDataMother.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import com.ofdun.jobfinder.features.education.data.postgres.repository.*;
import com.ofdun.jobfinder.features.experience.data.postgres.repository.*;
import com.ofdun.jobfinder.features.resume.data.postgres.mapper.ResumeMapper;
import java.util.*;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class PostgreSQLResumeRepositoryTest {
    @BeforeEach
    void setUpEntityManager() {
        org.springframework.test.util.ReflectionTestUtils.setField(
                repository, "entityManager", entityManager);
    }

    @Mock private jakarta.persistence.EntityManager entityManager;
    @Mock private jakarta.persistence.criteria.CriteriaBuilder criteria;

    @Mock
    private jakarta.persistence.criteria.CriteriaQuery<
                    com.ofdun.jobfinder.features.resume.data.postgres.entity.ResumeEntity>
            query;

    @Mock
    private jakarta.persistence.criteria.Root<
                    com.ofdun.jobfinder.features.resume.data.postgres.entity.ResumeEntity>
            root;

    @Mock
    private jakarta.persistence.TypedQuery<
                    com.ofdun.jobfinder.features.resume.data.postgres.entity.ResumeEntity>
            typed;

    @Test
    @Tag("equivalence")
    void searchResumes_whenAvailable_appliesOffsetAndLimit() {
        var expected = resume();
        when(storage.count(any(org.springframework.data.jpa.domain.Specification.class)))
                .thenReturn(3L);
        when(entityManager.getCriteriaBuilder()).thenReturn(criteria);
        when(criteria.createQuery(
                        com.ofdun.jobfinder.features.resume.data.postgres.entity.ResumeEntity
                                .class))
                .thenReturn(query);
        when(query.from(
                        com.ofdun.jobfinder.features.resume.data.postgres.entity.ResumeEntity
                                .class))
                .thenReturn(root);
        when(entityManager.createQuery(query)).thenReturn(typed);
        when(typed.getResultList()).thenReturn(List.of(ResumeMapper.toEntity(expected)));

        var result = repository.searchResumes(null, 2, 2, null, false);

        assertEquals(3L, result.getTotalElements());
        assertEquals(2, result.getTotalPages());
        assertEquals(1, result.getPage());
        assertEquals(2, result.getSize());
        assertEquals(expected.getDescription(), result.getItems().getFirst().getDescription());
        verify(typed).setFirstResult(2);
        verify(typed).setMaxResults(2);
    }

    @Test
    @Tag("equivalence")
    void searchResumes_whenStorageFails_propagatesFailure() {
        when(storage.count(any(org.springframework.data.jpa.domain.Specification.class)))
                .thenThrow(new IllegalStateException("offline"));

        var error =
                assertThrows(
                        IllegalStateException.class,
                        () -> repository.searchResumes(null, 2, 2, null, false));

        assertEquals("offline", error.getMessage());
        verifyNoInteractions(entityManager);
    }

    @Mock private ResumeJpaRepository storage;
    @Mock private EducationJpaRepository educations;
    @Mock private JobExperienceJpaRepository experiences;
    @InjectMocks private PostgreSQLResumeRepository repository;

    @Test
    @Tag("equivalence")
    void createResume_whenChildrenPresent_reassignsParentIds() {
        var model = resume();
        model.setEducations(List.of(education()));
        model.setJobExperiences(List.of(experience()));
        var saved = ResumeMapper.toEntity(model);
        saved.setId(42L);
        when(storage.save(any())).thenReturn(saved);

        var result = repository.createResume(model);

        assertEquals(42L, result);
        assertNull(model.getEducations().getFirst().getId());
        assertEquals(42L, model.getEducations().getFirst().getResumeId());
        assertNull(model.getJobExperiences().getFirst().getId());
        assertEquals(42L, model.getJobExperiences().getFirst().getResumeId());
        verify(educations).saveAll(any());
        verify(experiences).saveAll(any());
    }

    @Test
    @Tag("equivalence")
    void createResume_whenStorageFails_skipsChildren() {
        var model = resume();
        when(storage.save(any())).thenThrow(new IllegalStateException("offline"));

        var error = assertThrows(IllegalStateException.class, () -> repository.createResume(model));

        assertEquals("offline", error.getMessage());
        verifyNoInteractions(educations, experiences);
    }

    @Test
    @Tag("equivalence")
    void updateResume_whenChildrenPresent_reassignsParentIds() {
        var model = resume();
        model.setEducations(List.of(education()));
        model.setJobExperiences(List.of(experience()));
        var saved = ResumeMapper.toEntity(model);
        saved.setId(42L);
        when(storage.save(any())).thenReturn(saved);

        var result = repository.updateResume(model);

        assertEquals(42L, result.getId());
        assertNull(model.getEducations().getFirst().getId());
        assertEquals(42L, model.getEducations().getFirst().getResumeId());
        assertNull(model.getJobExperiences().getFirst().getId());
        assertEquals(42L, model.getJobExperiences().getFirst().getResumeId());
        verify(educations).saveAll(any());
        verify(experiences).saveAll(any());
    }

    @Test
    @Tag("equivalence")
    void updateResume_whenStorageFails_skipsChildren() {
        var model = resume();
        when(storage.save(any())).thenThrow(new IllegalStateException("offline"));

        var error = assertThrows(IllegalStateException.class, () -> repository.updateResume(model));

        assertEquals("offline", error.getMessage());
        verifyNoInteractions(educations, experiences);
    }

    @Test
    @Tag("equivalence")
    void getResumeById_whenPresent_loadsChildren() {
        var expected = resume();
        when(storage.findById(1L)).thenReturn(Optional.of(ResumeMapper.toEntity(expected)));
        when(educations.findAllByResumeId(1L))
                .thenReturn(
                        List.of(
                                com.ofdun.jobfinder.features.education.data.postgres.mapper
                                        .EducationMapper.toEntity(education())));
        when(experiences.findAllByResumeId(1L))
                .thenReturn(
                        List.of(
                                com.ofdun.jobfinder.features.experience.data.postgres.mapper
                                        .JobExperienceMapper.toEntity(experience())));

        var result = repository.getResumeById(1L).orElseThrow();

        assertEquals(expected.getDescription(), result.getDescription());
        assertEquals(List.of(education()), result.getEducations());
        assertEquals(List.of(experience()), result.getJobExperiences());
    }

    @Test
    @Tag("equivalence")
    void getResumeById_whenMissing_skipsChildren() {
        when(storage.findById(404L)).thenReturn(Optional.empty());

        var result = repository.getResumeById(404L);

        assertTrue(result.isEmpty());
        verifyNoInteractions(educations, experiences);
    }

    @Test
    @Tag("equivalence")
    void deleteResume_whenPresent_deletesChildrenBeforeParent() {
        var entity = ResumeMapper.toEntity(resume());
        when(storage.findById(1L)).thenReturn(Optional.of(entity));

        var result = repository.deleteResume(1L);

        assertTrue(result);
        var order = inOrder(educations, experiences, storage);
        order.verify(educations).deleteAllByResumeId(1L);
        order.verify(experiences).deleteAllByResumeId(1L);
        order.verify(storage).deleteById(1L);
    }

    @Test
    @Tag("equivalence")
    void deleteResume_whenMissing_returnsFalse() {
        when(storage.findById(404L)).thenReturn(Optional.empty());

        var result = repository.deleteResume(404L);

        assertFalse(result);
        verifyNoInteractions(educations, experiences);
    }
}
