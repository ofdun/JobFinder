package com.ofdun.jobfinder.features.resume.data.postgres.repository;

import static com.ofdun.jobfinder.support.TestDataMother.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import com.ofdun.jobfinder.features.clients.vector.VectorClient;
import io.qdrant.client.QdrantClient;
import io.qdrant.client.grpc.Points;
import java.util.*;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class QdrantResumeRepositoryTest {
    @Mock private VectorClient client;
    @Mock private QdrantClient qdrant;
    @InjectMocks private QdrantResumeRepository repository;

    @BeforeEach
    void setUp() {
        when(client.getClient()).thenReturn(qdrant);
        when(client.getCollectionName()).thenReturn("resumes");
    }

    @Test
    @Tag("equivalence")
    void createResume_whenAvailable_upsertsVector() {
        var model = resume();
        when(qdrant.upsertAsync(eq("resumes"), anyList()))
                .thenReturn(
                        com.google.common.util.concurrent.Futures.immediateFuture(
                                Points.UpdateResult.getDefaultInstance()));

        var result = repository.createResume(model);

        assertEquals(1L, result);
        verify(qdrant)
                .upsertAsync(
                        eq("resumes"),
                        argThat(
                                points ->
                                        points.size() == 1
                                                && points.getFirst().getId().getNum() == 1L));
    }

    @Test
    @Tag("equivalence")
    void createResume_whenRequestFails_wrapsFailure() {
        var model = resume();
        when(qdrant.upsertAsync(eq("resumes"), anyList()))
                .thenReturn(
                        com.google.common.util.concurrent.Futures.immediateFailedFuture(
                                new IllegalStateException("offline")));

        var error = assertThrows(RuntimeException.class, () -> repository.createResume(model));

        assertEquals("offline", error.getCause().getCause().getMessage());
    }

    @Test
    @Tag("equivalence")
    void updateResume_whenAvailable_upsertsVector() {
        var model = resume();
        when(qdrant.upsertAsync(eq("resumes"), anyList()))
                .thenReturn(
                        com.google.common.util.concurrent.Futures.immediateFuture(
                                Points.UpdateResult.getDefaultInstance()));

        var result = repository.updateResume(model);

        assertEquals(model, result);
        verify(qdrant)
                .upsertAsync(
                        eq("resumes"),
                        argThat(
                                points ->
                                        points.size() == 1
                                                && points.getFirst().getId().getNum() == 1L));
    }

    @Test
    @Tag("equivalence")
    void updateResume_whenRequestFails_wrapsFailure() {
        var model = resume();
        when(qdrant.upsertAsync(eq("resumes"), anyList()))
                .thenReturn(
                        com.google.common.util.concurrent.Futures.immediateFailedFuture(
                                new IllegalStateException("offline")));

        var error = assertThrows(RuntimeException.class, () -> repository.updateResume(model));

        assertEquals("offline", error.getCause().getCause().getMessage());
    }

    @Test
    @Tag("equivalence")
    void getResumeById_whenPresent_returnsEmbedding() {
        var point = mock(Points.RetrievedPoint.class, RETURNS_DEEP_STUBS);
        when(point.getVectors().getVector().getDense().getDataList())
                .thenReturn(List.of(0.1f, 0.2f));
        when(qdrant.retrieveAsync(
                        eq("resumes"),
                        eq(io.qdrant.client.PointIdFactory.id(1L)),
                        eq(true),
                        eq(true),
                        isNull()))
                .thenReturn(
                        com.google.common.util.concurrent.Futures.immediateFuture(List.of(point)));

        var result = repository.getResumeById(1L).orElseThrow();

        assertEquals(1L, result.getId());
        assertEquals(List.of(0.1f, 0.2f), result.getEmbedding());
    }

    @Test
    @Tag("equivalence")
    void getResumeById_whenMissing_returnsEmpty() {
        when(qdrant.retrieveAsync(
                        eq("resumes"),
                        eq(io.qdrant.client.PointIdFactory.id(404L)),
                        eq(true),
                        eq(true),
                        isNull()))
                .thenReturn(com.google.common.util.concurrent.Futures.immediateFuture(List.of()));

        var result = repository.getResumeById(404L);

        assertTrue(result.isEmpty());
    }

    @Test
    @Tag("equivalence")
    void getResumeById_whenRequestFails_wrapsFailure() {
        when(qdrant.retrieveAsync(
                        eq("resumes"),
                        eq(io.qdrant.client.PointIdFactory.id(1L)),
                        eq(true),
                        eq(true),
                        isNull()))
                .thenReturn(
                        com.google.common.util.concurrent.Futures.immediateFailedFuture(
                                new IllegalStateException("offline")));

        var error = assertThrows(RuntimeException.class, () -> repository.getResumeById(1L));

        assertEquals("offline", error.getCause().getCause().getMessage());
    }

    @Test
    @Tag("equivalence")
    void getMostSimilarResumes_whenAvailable_mapsScore() {
        var vector = List.of(0.1f, 0.2f);
        var point =
                Points.ScoredPoint.newBuilder()
                        .setId(io.qdrant.client.PointIdFactory.id(42L))
                        .setScore(0.9f)
                        .build();
        when(qdrant.searchAsync(any(Points.SearchPoints.class)))
                .thenReturn(
                        com.google.common.util.concurrent.Futures.immediateFuture(List.of(point)));

        var result = repository.getMostSimilarResumes(vector, 5);

        assertEquals(
                List.of(
                        new com.ofdun.jobfinder.features.matching.domain.model.MatchResultModel(
                                42L, 0.9f)),
                result);
        verify(qdrant)
                .searchAsync(
                        argThat(
                                request ->
                                        request.getLimit() == 5
                                                && request.getVectorList().equals(vector)));
    }

    @Test
    @Tag("equivalence")
    void getMostSimilarResumes_whenRequestFails_wrapsFailure() {
        var vector = List.of(0.1f);
        when(qdrant.searchAsync(any(Points.SearchPoints.class)))
                .thenReturn(
                        com.google.common.util.concurrent.Futures.immediateFailedFuture(
                                new IllegalStateException("offline")));

        var error =
                assertThrows(
                        RuntimeException.class, () -> repository.getMostSimilarResumes(vector, 5));

        assertEquals("offline", error.getCause().getCause().getMessage());
    }

    @Test
    @Tag("equivalence")
    void deleteResume_whenAvailable_returnsTrue() {
        when(qdrant.deleteAsync("resumes", List.of(io.qdrant.client.PointIdFactory.id(1L))))
                .thenReturn(
                        com.google.common.util.concurrent.Futures.immediateFuture(
                                Points.UpdateResult.getDefaultInstance()));

        var result = repository.deleteResume(1L);

        assertTrue(result);
    }

    @Test
    @Tag("equivalence")
    void deleteResume_whenRequestFails_returnsFalse() {
        when(qdrant.deleteAsync("resumes", List.of(io.qdrant.client.PointIdFactory.id(1L))))
                .thenReturn(
                        com.google.common.util.concurrent.Futures.immediateFailedFuture(
                                new IllegalStateException("offline")));

        var result = repository.deleteResume(1L);

        assertFalse(result);
    }
}
