package com.ofdun.jobfinder.features.resume.domain.chain.update;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import com.ofdun.jobfinder.features.resume.domain.model.ResumeModel;
import com.ofdun.jobfinder.features.resume.domain.repository.RelationalResumeRepository;
import java.util.Date;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.Test;

class RelationalResumeUpdateHandlerTest {

    @Test
    void execute_whenPartialUpdate_thenPreservesRequiredFieldsFromExistingResume() {
        RelationalResumeRepository repository = mock(RelationalResumeRepository.class);
        RelationalResumeUpdateHandler handler = new RelationalResumeUpdateHandler(repository);

        var existingDate = new Date();
        var existing = new ResumeModel();
        existing.setId(15L);
        existing.setApplicantId(4L);
        existing.setCategoryId(2L);
        existing.setDescription("Existing description");
        existing.setSkillIds(List.of(1L, 2L));
        existing.setLanguageIds(List.of(3L));
        existing.setDate(existingDate);

        var update = new ResumeModel();
        update.setId(15L);
        update.setDescription("Updated description");

        when(repository.getResumeById(15L)).thenReturn(Optional.of(existing));
        when(repository.updateResume(any(ResumeModel.class))).thenAnswer(invocation -> invocation.getArgument(0));

        var result = handler.handle(update);

        assertTrue(result.isPresent());
        var saved = result.get();
        assertEquals(4L, saved.getApplicantId());
        assertEquals(2L, saved.getCategoryId());
        assertEquals(existingDate, saved.getDate());
        assertEquals("Updated description", saved.getDescription());
    }
}

