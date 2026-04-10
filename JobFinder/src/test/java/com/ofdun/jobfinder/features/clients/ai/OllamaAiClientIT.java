package com.ofdun.jobfinder.features.clients.ai;

import static org.junit.jupiter.api.Assertions.*;

import com.ofdun.jobfinder.features.clients.ai.impl.OllamaAiClient;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.wait.strategy.Wait;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.utility.DockerImageName;

@ActiveProfiles("test")
@Testcontainers
class OllamaAiClientIT {

    private static final String PREBUILT_IMAGE = "ollama-mxbai-embed-large:latest";

    @Container
    static final GenericContainer<?> ollama;

    static {
        ollama = new GenericContainer<>(DockerImageName.parse(PREBUILT_IMAGE))
                .withExposedPorts(11434)
                .waitingFor(Wait.forListeningPort());
    }

    @DynamicPropertySource
    static void registerProperties(DynamicPropertyRegistry registry) {
        registry.add("app.ollama.url", () -> "http://" + ollama.getHost() + ":" + ollama.getMappedPort(11434));
    }

    @Test
    void getEmbedding_ReturnsVector_WhenModelAvailable() {
        String content = "test content for embedding";

        String base = "http://" + ollama.getHost() + ":" + ollama.getMappedPort(11434);
        OllamaAiClient client = new OllamaAiClient(base, "/api/embed", "mxbai-embed-large");

        List<Float> embedding = client.getEmbedding(content);

        assertNotNull(embedding);
        System.out.println(embedding);
        assertFalse(embedding.isEmpty());
    }
}
