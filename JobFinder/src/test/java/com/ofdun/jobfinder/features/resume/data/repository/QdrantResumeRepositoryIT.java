package com.ofdun.jobfinder.features.resume.data.repository;

import static org.junit.jupiter.api.Assertions.*;

import com.ofdun.jobfinder.features.clients.vector.configuration.VectorClientConfiguration;
import com.ofdun.jobfinder.features.resume.domain.model.ResumeModel;
import com.ofdun.jobfinder.features.resume.domain.repository.VectorResumeRepository;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.List;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.jdbc.test.autoconfigure.AutoConfigureTestDatabase;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.utility.DockerImageName;

@ActiveProfiles("test")
@SpringBootTest(
        properties = "spring.flyway.enabled=false",
        classes = {QdrantResumeRepository.class, VectorClientConfiguration.class})
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@Testcontainers
class QdrantResumeRepositoryIT {
    private static final String collectionName = "test-collection";
    private static final Integer embeddingDimension = 3;

    @Container
    static final GenericContainer<?> qdrant =
            new GenericContainer<>(DockerImageName.parse("qdrant/qdrant:latest"))
                    .withExposedPorts(6333, 6334);

    @DynamicPropertySource
    static void registerProperties(DynamicPropertyRegistry registry) {
        registry.add("app.qdrant.base", qdrant::getHost);
        registry.add("app.qdrant.grpc-port", () -> qdrant.getMappedPort(6334));
        registry.add("app.qdrant.rest-port", () -> qdrant.getMappedPort(6333));
        registry.add("app.qdrant.use-tls", () -> false);
        registry.add("app.qdrant.collection-name", () -> collectionName);
        registry.add("app.qdrant.embedding-dimension", () -> embeddingDimension);
    }

    @Autowired private VectorResumeRepository resumeRepository;

    @AfterEach
    void cleanup() {
        String host = qdrant.getHost();
        int port = qdrant.getMappedPort(6333);

        HttpClient httpClient = HttpClient.newHttpClient();

        String baseUrl = String.format("http://%s:%d/collections/%s", host, port, collectionName);

        try {
            HttpRequest deleteReq = HttpRequest.newBuilder()
                    .uri(URI.create(baseUrl))
                    .timeout(Duration.ofSeconds(5))
                    .DELETE()
                    .build();

            HttpResponse<String> deleteResp =
                    httpClient.send(deleteReq, HttpResponse.BodyHandlers.ofString());

            if (deleteResp.statusCode() != 200 && deleteResp.statusCode() != 404) {
                throw new RuntimeException("Failed to delete collection: " + deleteResp.body());
            }

            String body = String.format("""
                {
                  "vectors": {
                    "size": %d,
                    "distance": "Cosine"
                  }
                }
                """, embeddingDimension);

            HttpRequest createReq = HttpRequest.newBuilder()
                    .uri(URI.create(baseUrl))
                    .timeout(Duration.ofSeconds(5))
                    .PUT(HttpRequest.BodyPublishers.ofString(body))
                    .header("Content-Type", "application/json")
                    .build();

            HttpResponse<String> createResp =
                    httpClient.send(createReq, HttpResponse.BodyHandlers.ofString());

            if (createResp.statusCode() != 200) {
                throw new RuntimeException("Failed to create collection: " + createResp.body());
            }

        } catch (Exception e) {
            throw new RuntimeException("Failed to reset Qdrant collection", e);
        }
    }


    @Test
    void getMostSimilarResumes_whenTwoVectors_thenReturnClosest() {
        ResumeModel a = new ResumeModel(10L);
        a.setEmbedding(List.of(1.0f, 0.0f, 0.0f));
        ResumeModel b = new ResumeModel(11L);
        b.setEmbedding(List.of(0.0f, 1.0f, 0.0f));

        resumeRepository.createResume(a);
        resumeRepository.createResume(b);

        var results = resumeRepository.getMostSimilarResumes(List.of(0.9f, 0.1f, 0.0f), 2);

        assertNotNull(results);
        assertFalse(results.isEmpty());
        assertEquals(10L, results.getFirst().getId());
    }

    @Test
    void createResume_whenValid_thenGetById_returnsEmbedding() {
        ResumeModel model = new ResumeModel(1L);
        model.setEmbedding(List.of(1.0f, 0.0f, 0.0f));

        Long id = resumeRepository.createResume(model);

        assertNotNull(id);

        var fromDbOpt = resumeRepository.getResumeById(id);
        assertTrue(fromDbOpt.isPresent());
        assertEquals(3, fromDbOpt.get().getEmbedding().size());
        assertEquals(1.0f, fromDbOpt.get().getEmbedding().get(0));
    }

    @Test
    void updateResume_whenExists_thenUpdatedEmbedding() {
        ResumeModel model = new ResumeModel(2L);
        model.setEmbedding(List.of(0.0f, 1.0f, 0.0f));

        resumeRepository.createResume(model);

        model.setEmbedding(List.of(0.0f, 0.0f, 1.0f));

        ResumeModel updated = resumeRepository.updateResume(model);

        assertNotNull(updated);

        var fromDbOpt = resumeRepository.getResumeById(2L);
        assertTrue(fromDbOpt.isPresent());
        assertEquals(1.0f, fromDbOpt.get().getEmbedding().get(2));
    }

    @Test
    void deleteResume_whenExists_thenDeleted() {
        ResumeModel model = new ResumeModel(20L);
        model.setEmbedding(List.of(1.0f, 1.0f, 1.0f));

        resumeRepository.createResume(model);

        Boolean deleted = resumeRepository.deleteResume(20L);

        assertTrue(deleted);

        assertTrue(resumeRepository.getResumeById(20L).isEmpty());
    }
}
