package com.ofdun.jobfinder.features.clients.vector.configuration;

import com.ofdun.jobfinder.features.clients.vector.impl.BasicVectorClient;
import com.ofdun.jobfinder.features.clients.vector.properties.QdrantClientProperties;
import io.qdrant.client.QdrantClient;
import io.qdrant.client.QdrantGrpcClient;
import io.qdrant.client.grpc.Collections;
import io.qdrant.client.grpc.Collections.VectorParams;
import java.util.concurrent.ExecutionException;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class VectorClientConfiguration {

    @Bean
    public BasicVectorClient basicVectorClient(QdrantClientProperties properties)
            throws ExecutionException, InterruptedException {
        return new BasicVectorClient(qdrantClient(properties), properties.getCollectionName());
    }

    private QdrantClient qdrantClient(QdrantClientProperties properties)
            throws ExecutionException, InterruptedException {
        var grpcClientBuilder =
                QdrantGrpcClient.newBuilder(
                        properties.getBase(), properties.getGrpcPort(), properties.getUseTls());

        var client = new QdrantClient(grpcClientBuilder.build());

        if (!client.collectionExistsAsync(properties.getCollectionName()).get()) {
            client.createCollectionAsync(
                            properties.getCollectionName(),
                            defaultVectorParams(properties.getEmbeddingDimension()))
                    .get();
        }

        return client;
    }

    private static VectorParams defaultVectorParams(Integer dim) {
        return VectorParams.newBuilder()
                .setSize(dim)
                .setDistance(Collections.Distance.Cosine)
                .build();
    }
}
