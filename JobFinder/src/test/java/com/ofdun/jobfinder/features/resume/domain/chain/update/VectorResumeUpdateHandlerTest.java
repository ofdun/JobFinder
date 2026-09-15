package com.ofdun.jobfinder.features.resume.domain.chain.update;

import static com.ofdun.jobfinder.support.TestDataMother.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import com.ofdun.jobfinder.features.resume.domain.repository.VectorResumeRepository;
import java.util.*;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class VectorResumeUpdateHandlerTest {
    @Mock private VectorResumeRepository repository;
    @InjectMocks private VectorResumeUpdateHandler handler;

    @Test
    @Tag("equivalence")
    void handle_whenSuccessful_returnsProcessedResume() {
        var model = resume();
        when(repository.updateResume(model)).thenReturn(model);

        var result = handler.handle(model);

        assertTrue(result.isPresent());
        assertEquals(model, result.orElseThrow());
        verify(repository).updateResume(model);
    }

    @Test
    @Tag("equivalence")
    void handle_whenRepositoryFails_propagatesFailure() {
        var model = resume();
        when(repository.updateResume(model)).thenThrow(new IllegalStateException("offline"));

        var error = assertThrows(IllegalStateException.class, () -> handler.handle(model));

        assertEquals("offline", error.getMessage());
    }
}
