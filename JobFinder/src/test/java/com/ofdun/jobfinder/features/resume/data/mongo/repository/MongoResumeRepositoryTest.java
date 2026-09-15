package com.ofdun.jobfinder.features.resume.data.mongo.repository;

import static com.ofdun.jobfinder.support.TestDataMother.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import com.ofdun.jobfinder.features.education.data.mongo.repository.*;
import com.ofdun.jobfinder.features.experience.data.mongo.repository.*;
import com.ofdun.jobfinder.features.resume.data.mongo.mapper.ResumeMongoMapper;
import java.util.*;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class MongoResumeRepositoryTest {
    @Mock private MongoResumeCRUDRepository storage;
    @Mock private MongoEducationRepository educations;
    @Mock private MongoJobExperienceRepository experiences;
    @InjectMocks private MongoResumeRepository repository;

    @Test
    @Tag("equivalence")
    void createResume_whenChildrenPresent_reassignsParentIds() {
        var model = resume();
        model.setEducations(List.of(education()));
        model.setJobExperiences(List.of(experience()));
        var saved = ResumeMongoMapper.toEntity(model);
        saved.setId(42L);
        when(storage.save(any())).thenReturn(saved);

        var result = repository.createResume(model);

        assertEquals(42L, result);
        assertNull(model.getEducations().getFirst().getId());
        assertEquals(42L, model.getEducations().getFirst().getResumeId());
        assertNull(model.getJobExperiences().getFirst().getId());
        assertEquals(42L, model.getJobExperiences().getFirst().getResumeId());
        verify(educations).createEducations(model.getEducations());
        verify(experiences).createJobExperiences(model.getJobExperiences());
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
        var saved = ResumeMongoMapper.toEntity(model);
        saved.setId(42L);
        when(storage.save(any())).thenReturn(saved);

        var result = repository.updateResume(model);

        assertEquals(42L, result.getId());
        assertNull(model.getEducations().getFirst().getId());
        assertEquals(42L, model.getEducations().getFirst().getResumeId());
        assertNull(model.getJobExperiences().getFirst().getId());
        assertEquals(42L, model.getJobExperiences().getFirst().getResumeId());
        verify(educations).createEducations(model.getEducations());
        verify(experiences).createJobExperiences(model.getJobExperiences());
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
        when(storage.findById(1L)).thenReturn(Optional.of(ResumeMongoMapper.toEntity(expected)));
        when(educations.getEducationsByResumeId(1L)).thenReturn(List.of(education()));
        when(experiences.getJobExperiencesByResumeId(1L)).thenReturn(List.of(experience()));

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
        var entity = ResumeMongoMapper.toEntity(resume());
        when(storage.findById(1L)).thenReturn(Optional.of(entity));

        var result = repository.deleteResume(1L);

        assertTrue(result);
        var order = inOrder(educations, experiences, storage);
        order.verify(educations).deleteAllByResumeId(1L);
        order.verify(experiences).deleteAllByResumeId(1L);
        order.verify(storage).delete(entity);
    }

    @Test
    @Tag("equivalence")
    void deleteResume_whenMissing_returnsFalse() {
        when(storage.findById(404L)).thenReturn(Optional.empty());

        var result = repository.deleteResume(404L);

        assertFalse(result);
        verifyNoInteractions(educations, experiences);
    }

    @Test
    @Tag("equivalence")
    void searchResumes_whenMatching_returnsPage() {
        var model = resume();
        when(storage.findAll()).thenReturn(List.of(ResumeMongoMapper.toEntity(model)));
        var filter = new com.ofdun.jobfinder.features.resume.domain.model.ResumeSearchFilter();
        filter.setQ("  JAVA  ");

        var result = repository.searchResumes(filter, 10, 0, "id", false);

        assertEquals(1L, result.getTotalElements());
        assertEquals(model.getDescription(), result.getItems().getFirst().getDescription());
        assertEquals(1, result.getTotalPages());
    }

    @Test
    @Tag("equivalence")
    void searchResumes_whenNotMatching_returnsEmptyPage() {
        when(storage.findAll()).thenReturn(List.of(ResumeMongoMapper.toEntity(resume())));
        var filter = new com.ofdun.jobfinder.features.resume.domain.model.ResumeSearchFilter();
        filter.setApplicantId(999L);

        var result = repository.searchResumes(filter, 10, 0, "id", false);

        assertTrue(result.getItems().isEmpty());
        assertEquals(0L, result.getTotalElements());
    }
}
