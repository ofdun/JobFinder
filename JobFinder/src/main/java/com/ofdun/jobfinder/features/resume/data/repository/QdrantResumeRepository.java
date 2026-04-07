package com.ofdun.jobfinder.features.resume.data.repository;

import com.ofdun.jobfinder.features.clients.vector.VectorClient;
import com.ofdun.jobfinder.features.resume.domain.model.ResumeModel;
import com.ofdun.jobfinder.features.resume.domain.repository.VectorResumeRepository;
import com.ofdun.jobfinder.shared.matching.model.MatchResultModel;
import io.qdrant.client.PointIdFactory;
import io.qdrant.client.VectorsFactory;
import io.qdrant.client.grpc.Points;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class QdrantResumeRepository implements VectorResumeRepository {
    private final VectorClient client;

    @Override
    public Long createResume(ResumeModel resumeModel) {
        return saveResume(resumeModel).getId();
    }

    @Override
    public ResumeModel getResumeById(Long resumeId) {
        var model = new ResumeModel();

        List<Float> embedding;
        try {
            var resp =
                    client.getClient()
                            .retrieveAsync(
                                    client.getCollectionName(),
                                    PointIdFactory.id(resumeId),
                                    true,
                                    true,
                                    null)
                            .get();
            embedding = resp.getFirst().getVectors().getVector().getDense().getDataList();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

        model.setEmbedding(embedding);

        return model;
    }

    @Override
    public ResumeModel updateResume(ResumeModel resumeModel) {
        return saveResume(resumeModel);
    }

    @Override
    public List<MatchResultModel> getMostSimilarResumes(List<Float> embedding, Integer maxAmount) {
        try {
            var points =
                    client.getClient()
                            .searchAsync(
                                    Points.SearchPoints.newBuilder()
                                            .setCollectionName(client.getCollectionName())
                                            .addAllVector(embedding)
                                            .setLimit(maxAmount)
                                            .build())
                            .get();
            return points.stream()
                    .map(p -> new MatchResultModel(p.getId().getNum(), p.getScore()))
                    .toList();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public Boolean deleteResume(Long resumeId) {
        var id = PointIdFactory.id(resumeId);

        var res = true;
        try {
            client.getClient().deleteAsync(client.getCollectionName(), List.of(id)).get();
        } catch (Exception e) {
            res = false;
        }

        return res;
    }

    private ResumeModel saveResume(ResumeModel resumeModel) {
        var id = PointIdFactory.id(resumeModel.getId());
        var vectors = VectorsFactory.vectors(resumeModel.getEmbedding());

        try {
            client.getClient()
                    .upsertAsync(
                            client.getCollectionName(),
                            List.of(
                                    Points.PointStruct.newBuilder()
                                            .setId(id)
                                            .setVectors(vectors)
                                            .build()))
                    .get();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

        return resumeModel;
    }
}
