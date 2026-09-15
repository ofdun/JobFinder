package com.ofdun.jobfinder.features.resume.domain.chain.save;

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
class RelationalResumeSaveHandlerTest {
    @Mock private RelationalResumeRepository repository;
    @InjectMocks private RelationalResumeSaveHandler handler;

    @Test
    @Tag("equivalence")
    void handle_whenSuccessful_returnsProcessedResume() {
        var model = resume();
        when(repository.createResume(model)).thenReturn(42L);

        var result = handler.handle(model);

        assertTrue(result.isPresent());
        assertEquals(42L, result.orElseThrow().getId());
        verify(repository).createResume(model);
    }

    @Test
    @Tag("equivalence")
    void handle_whenRepositoryFails_propagatesFailure() {
        var model = resume();
        when(repository.createResume(model)).thenThrow(new IllegalStateException("offline"));

        var error = assertThrows(IllegalStateException.class, () -> handler.handle(model));

        assertEquals("offline", error.getMessage());
    }
}
