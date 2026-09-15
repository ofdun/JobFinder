package com.ofdun.jobfinder.features.resume.domain.chain.get;

import static com.ofdun.jobfinder.support.TestDataMother.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import com.ofdun.jobfinder.features.resume.domain.repository.RelationalResumeRepository;
import java.util.*;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class RelationalResumeGetHandlerTest {
    @Mock private RelationalResumeRepository repository;
    @InjectMocks private RelationalResumeGetHandler handler;

    @Test
    @Tag("equivalence")
    void handle_whenSuccessful_returnsProcessedResume() {
        var model = resume();
        var stored = resume();
        when(repository.getResumeById(1L)).thenReturn(Optional.of(stored));

        var result = handler.handle(model);

        assertTrue(result.isPresent());
        assertEquals(stored.getEmbedding(), result.orElseThrow().getEmbedding());
    }

    @Test
    @Tag("equivalence")
    void handle_whenRepositoryFails_propagatesFailure() {
        var model = resume();
        when(repository.getResumeById(model.getId()))
                .thenThrow(new IllegalStateException("offline"));

        var error = assertThrows(IllegalStateException.class, () -> handler.handle(model));

        assertEquals("offline", error.getMessage());
    }

    @Test
    @Tag("equivalence")
    void handle_whenMissing_handlesAbsence() {
        var model = resume();
        when(repository.getResumeById(1L)).thenReturn(Optional.empty());

        var result = handler.handle(model);

        assertTrue(result.isEmpty());
    }
}
