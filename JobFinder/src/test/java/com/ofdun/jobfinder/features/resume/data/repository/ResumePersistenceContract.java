package com.ofdun.jobfinder.features.resume.data.repository;

import static org.junit.jupiter.api.Assertions.*;

import com.ofdun.jobfinder.features.resume.domain.model.ResumeModel;
import com.ofdun.jobfinder.features.resume.domain.model.ResumeSearchFilter;
import com.ofdun.jobfinder.features.resume.domain.repository.RelationalResumeRepository;
import com.ofdun.jobfinder.support.TestDataMother;
import java.util.*;

/** The same persistence guarantees must hold for both supported databases. */
interface ResumePersistenceContract {
    RelationalResumeRepository repository();

    default void flushAndClear() {}

    void assertNoChildren(long id);

    default ResumeModel populatedResume() {
        var model = TestDataMother.resume();
        model.setId(null);
        model.setApplicantId(1L);
        model.setCategoryId(1L);
        model.setSkillIds(List.of(1L));
        var education = TestDataMother.education();
        education.setId(null);
        var experience = TestDataMother.experience();
        experience.setId(null);
        experience.setEndDate(null);
        model.setEducations(List.of(education));
        model.setJobExperiences(List.of(experience));
        return model;
    }

    default ResumeModel save(ResumeModel model) {
        long id = repository().createResume(model);
        flushAndClear();
        return repository().getResumeById(id).orElseThrow();
    }

    default void nestedRecordsRoundTripWithCurrentEmployment() {
        var stored = save(populatedResume());
        assertEquals(1, stored.getEducations().size());
        assertEquals(1, stored.getJobExperiences().size());
        assertEquals(stored.getId(), stored.getEducations().getFirst().getResumeId());
        assertEquals(stored.getId(), stored.getJobExperiences().getFirst().getResumeId());
        assertEquals("BMSTU", stored.getEducations().getFirst().getInstitutionName());
        assertNull(stored.getJobExperiences().getFirst().getEndDate());
        assertEquals(
                java.time.LocalDate.of(2023, 1, 1),
                stored.getJobExperiences().getFirst().getStartDate());
    }

    default void replacingWithEmptyCollectionsRemovesStoredRelations() {
        var stored = save(populatedResume());
        stored.setDescription(null);
        stored.setEducations(List.of());
        stored.setJobExperiences(List.of());
        stored.setSkillIds(List.of());
        stored.setLanguageIds(List.of());
        repository().updateResume(stored);
        flushAndClear();
        var reread = repository().getResumeById(stored.getId()).orElseThrow();
        assertNull(reread.getDescription());
        assertTrue(reread.getEducations() == null || reread.getEducations().isEmpty());
        assertTrue(reread.getJobExperiences() == null || reread.getJobExperiences().isEmpty());
        assertTrue(reread.getSkillIds() == null || reread.getSkillIds().isEmpty());
        assertTrue(reread.getLanguageIds() == null || reread.getLanguageIds().isEmpty());
        assertNoChildren(stored.getId());
    }

    default void replacingChildrenDoesNotAppendOrModifyAnotherResume() {
        var first = save(populatedResume());
        var second = save(populatedResume());
        var education = TestDataMother.education();
        education.setInstitutionName("Replacement university");
        var experience = TestDataMother.experience();
        experience.setCompanyName("Replacement company");
        // Incoming nested IDs must not let one resume overwrite another's records.
        education.setId(second.getEducations().getFirst().getId());
        experience.setId(second.getJobExperiences().getFirst().getId());
        first.setEducations(List.of(education));
        first.setJobExperiences(List.of(experience));
        repository().updateResume(first);
        repository().updateResume(first);
        flushAndClear();
        var updated = repository().getResumeById(first.getId()).orElseThrow();
        assertEquals(1, updated.getEducations().size());
        assertEquals(1, updated.getJobExperiences().size());
        assertEquals(
                "Replacement university", updated.getEducations().getFirst().getInstitutionName());
        assertEquals(
                "Replacement company", updated.getJobExperiences().getFirst().getCompanyName());
        var untouched = repository().getResumeById(second.getId()).orElseThrow();
        assertEquals("BMSTU", untouched.getEducations().getFirst().getInstitutionName());
        assertEquals("Example Ltd", untouched.getJobExperiences().getFirst().getCompanyName());
    }

    default void deletingResumeRemovesItsChildrenOnly() {
        var first = save(populatedResume());
        var second = save(populatedResume());
        assertTrue(repository().deleteResume(first.getId()));
        flushAndClear();
        assertTrue(repository().getResumeById(first.getId()).isEmpty());
        assertNoChildren(first.getId());
        var untouched = repository().getResumeById(second.getId()).orElseThrow();
        assertEquals(1, untouched.getEducations().size());
        assertEquals(1, untouched.getJobExperiences().size());
        assertFalse(repository().deleteResume(first.getId()));
    }

    default void paginationWithEqualDatesHasStableOrderAndFullCount() {
        var ids = new ArrayList<Long>();
        for (int i = 0; i < 3; i++) {
            var model = populatedResume();
            model.setDescription("pagination-contract");
            ids.add(save(model).getId());
        }
        var filter =
                new ResumeSearchFilter("pagination-contract", 1L, null, null, null, null, null);
        var first = repository().searchResumes(filter, 1, 0, "date", true);
        var second = repository().searchResumes(filter, 1, 1, "date", true);
        var beyond = repository().searchResumes(filter, 1, 3, "date", true);
        assertEquals(3L, first.getTotalElements());
        assertEquals(3, first.getTotalPages());
        assertEquals(ids.get(2), first.getItems().getFirst().getId());
        assertEquals(ids.get(1), second.getItems().getFirst().getId());
        assertTrue(beyond.getItems().isEmpty());
        assertEquals(3L, beyond.getTotalElements());
    }

    default void ownershipFilterCannotReturnOtherApplicantsResumes() {
        var mine = populatedResume();
        mine.setDescription("ownership-contract");
        long ownId = save(mine).getId();
        var other = populatedResume();
        other.setApplicantId(2L);
        other.setDescription("ownership-contract");
        save(other);
        var filter = new ResumeSearchFilter("ownership-contract", 1L, null, null, null, null, null);
        var result = repository().searchResumes(filter, 20, 0, "id", false);
        assertEquals(1L, result.getTotalElements());
        assertEquals(List.of(ownId), result.getItems().stream().map(ResumeModel::getId).toList());
    }
}
