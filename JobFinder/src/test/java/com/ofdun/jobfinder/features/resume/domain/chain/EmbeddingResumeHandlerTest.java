package com.ofdun.jobfinder.features.resume.domain.chain;

import static com.ofdun.jobfinder.support.TestDataMother.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import com.ofdun.jobfinder.features.clients.ai.AiClient;
import java.util.*;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class EmbeddingResumeHandlerTest {
    @Mock private AiClient client;
    @InjectMocks private EmbeddingResumeHandler handler;

    @Test
    @Tag("equivalence")
    void handle_whenEmbeddingAvailable_updatesModel() {
        var model = resume();
        var vector = List.of(0.7f, 0.8f);
        when(client.getEmbedding(model.toString())).thenReturn(vector);

        var result = handler.handle(model);

        assertEquals(vector, result.orElseThrow().getEmbedding());
    }

    @Test
    @Tag("equivalence")
    void handle_whenClientFails_propagatesFailure() {
        var model = resume();
        when(client.getEmbedding(model.toString())).thenThrow(new IllegalStateException("offline"));

        var error = assertThrows(IllegalStateException.class, () -> handler.handle(model));

        assertEquals("offline", error.getMessage());
    }
}
