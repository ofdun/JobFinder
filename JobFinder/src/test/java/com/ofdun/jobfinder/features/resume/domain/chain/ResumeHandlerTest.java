package com.ofdun.jobfinder.features.resume.domain.chain;

import static com.ofdun.jobfinder.support.TestDataMother.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import com.ofdun.jobfinder.features.resume.domain.chain.update.VectorResumeUpdateHandler;
import com.ofdun.jobfinder.features.resume.domain.repository.VectorResumeRepository;
import java.util.*;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class ResumeHandlerTest {
    @Mock private VectorResumeRepository repository;
    private ResumeHandler handler;

    @BeforeEach
    void setUp() {
        handler = new VectorResumeUpdateHandler(repository);
    }

    @Test
    @Tag("equivalence")
    void setNext_whenHandlerProvided_returnsNext() {
        var next = mock(ResumeHandler.class);

        var result = handler.setNext(next);

        assertSame(next, result);
    }

    @Test
    @Tag("equivalence")
    void setNext_whenNull_detachesNext() {
        handler.setNext(mock(ResumeHandler.class));

        var result = handler.setNext(null);

        assertNull(result);
    }

    @Test
    @Tag("equivalence")
    void handle_whenStageSucceeds_passesResultToNext() {
        var model = resume();
        var next = mock(ResumeHandler.class);
        handler.setNext(next);
        when(repository.updateResume(model)).thenReturn(model);
        when(next.handle(model)).thenReturn(Optional.of(model));

        var result = handler.handle(model);

        assertEquals(Optional.of(model), result);
        verify(next).handle(model);
    }

    @Test
    @Tag("equivalence")
    void handle_whenStageReturnsEmpty_skipsNext() {
        var model = resume();
        var next = mock(ResumeHandler.class);
        handler.setNext(next);
        when(repository.updateResume(model)).thenReturn(null);

        var result = handler.handle(model);

        assertTrue(result.isEmpty());
        verifyNoInteractions(next);
    }
}
